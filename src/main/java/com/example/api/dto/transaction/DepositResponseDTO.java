package com.example.api.dto.transaction;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DepositResponseDTO {
    private List<String> message;
}