package com.example.api.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Data
public class JwtResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;

    private List<String> message;

}