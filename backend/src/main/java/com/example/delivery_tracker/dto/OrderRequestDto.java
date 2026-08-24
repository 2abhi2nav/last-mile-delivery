package com.example.delivery_tracker.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderRequestDto {
    private String originPincode;
    private String destinationPincode;
    private String orderType; // STANDARD, EXPRESS
    private boolean isCod;
    private BigDecimal weightKg;
    private BigDecimal lengthCm;
    private BigDecimal breadthCm;
    private BigDecimal heightCm;
    private Long customerId; // Optional: if admin is creating on behalf of a customer
}
