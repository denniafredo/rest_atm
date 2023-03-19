package com.example.api.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JwtLogoutResponseDTO implements Serializable {
    @Serial
    private String message;
}