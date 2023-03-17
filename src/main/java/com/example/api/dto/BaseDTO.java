package com.example.api.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class BaseDTO {
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;
}
