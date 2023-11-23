package com.example.digitalbox.controller;


import java.util.*;
import java.util.stream.Collectors;

import com.example.digitalbox.security.service.EmailService;
import com.example.digitalbox.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import com.example.digitalbox.model.ERole;
import com.example.digitalbox.model.Role;
import com.example.digitalbox.model.User;
import com.example.digitalbox.payload.Request.LoginRequest;
import com.example.digitalbox.payload.Request.SignupRequest;
import com.example.digitalbox.payload.Response.JwtResponse;
import com.example.digitalbox.payload.Response.MessageResponse;
import com.example.digitalbox.repository.RoleRepository;
import com.example.digitalbox.repository.UserRepository;
import com.example.digitalbox.security.jwt.JwtUtils;
import com.example.digitalbox.security.service.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findUserByEmail(loginRequest.getEmail());
        if(user.isEnabled()) {
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            return ResponseEntity
                    .ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), roles));

        }
        else {
            System.out.println("You must first activate your account");
            return  ResponseEntity.status(503).body(HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<String> verifyUser(@RequestParam String email, @RequestParam String code) {
        boolean verified = userService.verifyUser(email, code);
        if (verified) {
            return new ResponseEntity<>("User verified", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Verification failed", HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            String verificationCode = UUID.randomUUID().toString();

            if (userRepository.existsByPhone(signUpRequest.getPhone())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Phone number is already taken!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            User user = new User(signUpRequest.getEmail(), signUpRequest.getFullName(), signUpRequest.getPhone(),
                    encoder.encode(signUpRequest.getPassword()));

            Set<String> strRoles = signUpRequest.getRoles();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "manager":
                            Role adminRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(adminRole);

                            break;
                        case "driver":
                            Role modRole = roleRepository.findByName(ERole.ROLE_DRIVER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(modRole);

                            break;
                        default:
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(userRole);
                    }
                });
            }

            user.setRoles(roles);
            user.setEnabled(false);
            user.setVerificationToken(verificationCode);
            if(emailService.sendAccountActivationLink(user))
                userRepository.save(user);
            else
                throw new RuntimeException("failed to create user");

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            throw new RuntimeException("failed to create user");
        }
    }
}
