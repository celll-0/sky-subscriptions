package com.sky.subscription.controller;

import com.sky.subscription.dto.SubscriptionDto;
import com.sky.subscription.entity.SubscriptionStatus;
import com.sky.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDto> getSubscriptionById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByCustomerId(@PathVariable Integer customerId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByStatus(@PathVariable SubscriptionStatus status) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody SubscriptionDto dto) {
        SubscriptionDto created = subscriptionService.createSubscription(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> updateSubscription(
            @PathVariable Integer id,
            @Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Integer id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    // Add-on management
    @PostMapping("/{subscriptionId}/addons/{addonId}")
    public ResponseEntity<SubscriptionDto> addAddOnToSubscription(
            @PathVariable Integer subscriptionId,
            @PathVariable Integer addonId) {
        return ResponseEntity.ok(subscriptionService.addAddOnToSubscription(subscriptionId, addonId));
    }

    @DeleteMapping("/{subscriptionId}/addons/{addonId}")
    public ResponseEntity<Void> removeAddOnFromSubscription(
            @PathVariable Integer subscriptionId,
            @PathVariable Integer addonId) {
        subscriptionService.removeAddOnFromSubscription(subscriptionId, addonId);
        return ResponseEntity.noContent().build();
    }

    // App tier management
    @PostMapping("/{subscriptionId}/apps/{appTierId}")
    public ResponseEntity<SubscriptionDto> addAppTierToSubscription(
            @PathVariable Integer subscriptionId,
            @PathVariable Integer appTierId) {
        return ResponseEntity.ok(subscriptionService.addAppTierToSubscription(subscriptionId, appTierId));
    }

    @DeleteMapping("/{subscriptionId}/apps/{appTierId}")
    public ResponseEntity<Void> removeAppTierFromSubscription(
            @PathVariable Integer subscriptionId,
            @PathVariable Integer appTierId) {
        subscriptionService.removeAppTierFromSubscription(subscriptionId, appTierId);
        return ResponseEntity.noContent().build();
    }
}
