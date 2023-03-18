package com.example.api.dto.debt;

import com.example.api.model.User;

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
public class CreateDebtDTO {
    private User owes;
    private User owed;
    private Double amount;
}