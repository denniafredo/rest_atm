package com.example.api.dto.transaction;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WithdrawResponseDTO {
    private List<String> message;
}