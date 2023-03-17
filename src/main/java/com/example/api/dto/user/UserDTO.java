package com.example.api.dto.user;

import com.example.api.dto.BaseDTO;

import lombok.Data;

@Data
public class UserDTO extends BaseDTO {
    private long id;
    private String username;
    private String passwordHash;
    private String accountNumber;
    private boolean isActive;
}