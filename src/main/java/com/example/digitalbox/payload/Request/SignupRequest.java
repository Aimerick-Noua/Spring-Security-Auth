package com.example.digitalbox.payload.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
public class SignupRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private Set<String> roles;

}