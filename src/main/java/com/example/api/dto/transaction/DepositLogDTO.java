package com.example.api.dto.transaction;

import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class DepositLogDTO {

    @Enumerated
    private String Status;
}