package com.example.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Builder
@AllArgsConstructor
public class CreateUserDTO {
    private String username;
    private String password;
    private String accountNumber;
}