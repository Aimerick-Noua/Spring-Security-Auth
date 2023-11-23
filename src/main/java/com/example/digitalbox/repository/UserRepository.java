package com.example.digitalbox.repository;


import java.util.Optional;

import com.example.digitalbox.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByPhone(String username);

    Boolean existsByEmail(String email);

    User findUserById(Long id);
    User findUserByEmail(String email);
}
