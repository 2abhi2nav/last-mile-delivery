package com.example.delivery_tracker.service;

import com.example.delivery_tracker.model.CodSurcharge;
import com.example.delivery_tracker.model.RateCard;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateCalculationServiceTest {

    private final RateCalculationService service = new RateCalculationService();

    @Test
    void testVolumetricWeightFormula() {
        // (50cm * 40cm * 30cm) / 5000 = 60000 / 5000 = 12.00 kg
        BigDecimal volWeight = service.calculateVolumetricWeight(
                new BigDecimal("50"), new BigDecimal("40"), new BigDecimal("30")
        );
        assertEquals(new BigDecimal("12.00"), volWeight);
    }

    @Test
    void testBillableWeightTiebreakActualGreater() {
        BigDecimal actual = new BigDecimal("15.50");
        BigDecimal volumetric = new BigDecimal("10.00");
        BigDecimal billable = service.calculateBillableWeight(actual, volumetric);
        assertEquals(new BigDecimal("15.50"), billable);
    }

    @Test
    void testBillableWeightTiebreakVolumetricGreater() {
        BigDecimal actual = new BigDecimal("5.00");
        BigDecimal volumetric = new BigDecimal("12.50");
        BigDecimal billable = service.calculateBillableWeight(actual, volumetric);
        assertEquals(new BigDecimal("12.50"), billable);
    }

    @Test
    void testShippingCostNonCod() {
        RateCard rateCard = RateCard.builder()
                .baseRate(new BigDecimal("50.00"))
                .perKgRate(new BigDecimal("10.00"))
                .build();

        // Billable weight = 3 kg -> 50 + (3 * 10) = 80.00
        BigDecimal cost = service.calculateShippingCost(new BigDecimal("3.00"), rateCard, false, null);
        assertEquals(new BigDecimal("80.00"), cost);
    }

    @Test
    void testShippingCostWithCodSurcharge() {
        RateCard rateCard = RateCard.builder()
                .baseRate(new BigDecimal("50.00"))
                .perKgRate(new BigDecimal("10.00"))
                .build();

        CodSurcharge surcharge = CodSurcharge.builder()
                .surchargeAmount(new BigDecimal("25.00"))
                .build();

        // Billable weight = 3 kg -> 50 + (3 * 10) + 25 = 105.00
        BigDecimal cost = service.calculateShippingCost(new BigDecimal("3.00"), rateCard, true, surcharge);
        assertEquals(new BigDecimal("105.00"), cost);
    }
}
