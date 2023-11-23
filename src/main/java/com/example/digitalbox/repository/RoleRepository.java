package com.example.digitalbox.repository;


import java.util.Optional;

import com.example.digitalbox.model.ERole;
import com.example.digitalbox.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
    Boolean existsByName(ERole name);
}