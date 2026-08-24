package com.example.delivery_tracker.controller;

import com.example.delivery_tracker.model.CodSurcharge;
import com.example.delivery_tracker.model.RateCard;
import com.example.delivery_tracker.model.Zone;
import com.example.delivery_tracker.model.ZoneArea;
import com.example.delivery_tracker.service.AdminManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminManagementService adminManagementService;

    // Zones
    @PostMapping("/zones")
    public ResponseEntity<Zone> createZone(@RequestParam String name) {
        return ResponseEntity.ok(adminManagementService.createZone(name));
    }

    @GetMapping("/zones")
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(adminManagementService.getAllZones());
    }

    // Zone Areas (Pincodes)
    @PostMapping("/zone-areas")
    public ResponseEntity<ZoneArea> assignPincodeToZone(@RequestParam String pincode, @RequestParam String zoneName) {
        return ResponseEntity.ok(adminManagementService.assignPincodeToZone(pincode, zoneName));
    }

    @GetMapping("/zone-areas")
    public ResponseEntity<List<ZoneArea>> getAllZoneAreas() {
        return ResponseEntity.ok(adminManagementService.getAllZoneAreas());
    }

    // Rate Cards
    @PostMapping("/rate-cards")
    public ResponseEntity<RateCard> saveRateCard(@RequestBody RateCard rateCard) {
        return ResponseEntity.ok(adminManagementService.saveRateCard(rateCard));
    }

    @GetMapping("/rate-cards")
    public ResponseEntity<List<RateCard>> getAllRateCards() {
        return ResponseEntity.ok(adminManagementService.getAllRateCards());
    }

    // COD Surcharges
    @PostMapping("/cod-surcharges")
    public ResponseEntity<CodSurcharge> saveCodSurcharge(@RequestBody CodSurcharge codSurcharge) {
        return ResponseEntity.ok(adminManagementService.saveCodSurcharge(codSurcharge));
    }

    @GetMapping("/cod-surcharges")
    public ResponseEntity<List<CodSurcharge>> getAllCodSurcharges() {
        return ResponseEntity.ok(adminManagementService.getAllCodSurcharges());
    }
}
