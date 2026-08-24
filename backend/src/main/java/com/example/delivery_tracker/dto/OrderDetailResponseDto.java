package com.example.delivery_tracker.dto;

import com.example.delivery_tracker.model.Order;
import com.example.delivery_tracker.model.OrderStatusHistory;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderDetailResponseDto {
    private Order order;
    private List<OrderStatusHistory> timeline;
}
