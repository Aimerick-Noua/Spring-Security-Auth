package com.example.digitalbox.service;

import com.example.digitalbox.model.ERole;
import com.example.digitalbox.model.Role;
import com.example.digitalbox.model.User;
import com.example.digitalbox.repository.RoleRepository;
import com.example.digitalbox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
        public void initRoleAndUser(){

            if (!roleRepository.existsByName((ERole.ROLE_MANAGER))) {
                Role managerRole = new Role();
                managerRole.setName(ERole.ROLE_MANAGER);
                roleRepository.save(managerRole);

                Role driverRole = new Role();
                driverRole.setName(ERole.ROLE_DRIVER);
                roleRepository.save(driverRole);

                Role userRole = new Role();
                userRole.setName(ERole.ROLE_USER);
                roleRepository.save(userRole);
            }

    }

    public List<User> getAllUsers() {
            return userRepository.findAll();
    }

    public User getUserById(Long id) {
            if(userRepository.existsById(id)){
                return userRepository.findUserById(id);
            }
            return null;
    }

    public void deleteUser(Long id) {
            userRepository.deleteById(id);
    }

    public User updateUser(Long userId, String fullName, String email, String password, String phone, String location, String nationalIdentityNumber, MultipartFile profilePicture) {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(getEncodedPassword(password));
            user.setPhone(phone);
            user.setLocation(location);
            user.setNational_identity_number(nationalIdentityNumber);

            if (profilePicture != null && !profilePicture.isEmpty()) {
                String profilePicturePath = saveProfilePicture(profilePicture);
                user.setProfilePicture(profilePicturePath);
            }

            return userRepository.save(user);
        }

        return null;
    }
    private String saveProfilePicture(MultipartFile profilePicture) {
        String fileName = UUID.randomUUID().toString() + "_" + profilePicture.getOriginalFilename();
        String filePath = "E:/DigitalBox-backend/src/main/resources/profilePictures" + fileName;

        try {
            Files.copy(profilePicture.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        } catch (IOException e) {
            return null;
        }
    }

    public String getEncodedPassword(String password){
        return passwordEncoder.encode(password);
    }

    public boolean verifyUser(String email, String verificationCode) {
        User user = userRepository.findUserByEmail(email);
        if (user != null && user.getVerificationToken().equals(verificationCode)) {
            user.setEnabled(true);
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }
}
