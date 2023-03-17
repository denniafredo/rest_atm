package com.example.api.dto.transaction;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class WithdrawDTO {
    private Double amount;
}