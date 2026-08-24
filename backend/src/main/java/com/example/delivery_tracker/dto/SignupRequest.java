package com.example.delivery_tracker.dto;

import com.example.delivery_tracker.model.User;
import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private User.Role role;
}
