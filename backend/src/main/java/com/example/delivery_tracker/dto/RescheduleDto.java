package com.example.delivery_tracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RescheduleDto {
    private LocalDateTime newDeliveryDate;
    private String reason;
}
