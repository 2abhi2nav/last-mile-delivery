package com.example.delivery_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPreviewResponseDto {
    private String originZone;
    private String destinationZone;
    private String orderType;
    private boolean isCod;
    private BigDecimal actualWeightKg;
    private BigDecimal volumetricWeightKg;
    private BigDecimal billableWeightKg;
    private BigDecimal baseRate;
    private BigDecimal perKgRate;
    private BigDecimal codSurcharge;
    private BigDecimal shippingCost;
}
