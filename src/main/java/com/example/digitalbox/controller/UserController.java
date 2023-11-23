package com.example.digitalbox.controller;

import com.example.digitalbox.model.User;
import com.example.digitalbox.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
private final UserService userService;
    @PostConstruct
    public void initRoleAndUsers(){
        userService.initRoleAndUser();
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER') || hasRole('ROLE_DRIVER')")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_MANAGER') || hasRole('ROLE_USER') || hasRole('ROLE_DRIVER')")
    public User getUserById(@PathVariable Long userId){
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_MANAGER') || hasRole('ROLE_USER') || hasRole('ROLE_DRIVER')")

    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String location,
            @RequestParam String national_identity_number,
            @RequestParam(required = false) MultipartFile profilePicture
    ) {
        User updatedUser = userService.updateUser(userId, fullName, email,password,phone,location,national_identity_number, profilePicture);

        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void deleteUser(@PathVariable Long userId){
         userService.deleteUser(userId);
    }

}
