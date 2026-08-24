package com.example.delivery_tracker.dto;

import com.example.delivery_tracker.model.Order;
import lombok.Data;

@Data
public class StatusUpdateDto {
    private Order.OrderStatus status;
}
