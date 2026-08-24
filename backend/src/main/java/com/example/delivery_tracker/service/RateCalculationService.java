package com.example.delivery_tracker.service;

import com.example.delivery_tracker.model.CodSurcharge;
import com.example.delivery_tracker.model.RateCard;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RateCalculationService {

    public BigDecimal calculateVolumetricWeight(BigDecimal length, BigDecimal breadth, BigDecimal height) {
        if (length == null || breadth == null || height == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return length.multiply(breadth).multiply(height)
                .divide(new BigDecimal("5000"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateBillableWeight(BigDecimal actualWeight, BigDecimal volumetricWeight) {
        BigDecimal act = actualWeight != null ? actualWeight : BigDecimal.ZERO;
        BigDecimal vol = volumetricWeight != null ? volumetricWeight : BigDecimal.ZERO;
        return act.max(vol).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateShippingCost(BigDecimal billableWeight, RateCard rateCard, boolean isCod, CodSurcharge codSurcharge) {
        if (rateCard == null) {
            throw new IllegalArgumentException("Rate card cannot be null for cost calculation");
        }

        BigDecimal base = rateCard.getBaseRate() != null ? rateCard.getBaseRate() : BigDecimal.ZERO;
        BigDecimal perKg = rateCard.getPerKgRate() != null ? rateCard.getPerKgRate() : BigDecimal.ZERO;
        BigDecimal bw = billableWeight != null ? billableWeight : BigDecimal.ZERO;

        BigDecimal cost = base.add(bw.multiply(perKg));

        if (isCod && codSurcharge != null && codSurcharge.getSurchargeAmount() != null) {
            cost = cost.add(codSurcharge.getSurchargeAmount());
        }

        return cost.setScale(2, RoundingMode.HALF_UP);
    }
}
