package com.sky.subscription.controller;

import com.sky.subscription.dto.AppDto;
import com.sky.subscription.dto.AppTierDto;
import com.sky.subscription.service.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    // App endpoints
    @GetMapping
    public ResponseEntity<List<AppDto>> getAllApps() {
        return ResponseEntity.ok(appService.getAllApps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppDto> getAppById(@PathVariable Integer id) {
        return ResponseEntity.ok(appService.getAppById(id));
    }

    @PostMapping
    public ResponseEntity<AppDto> createApp(@Valid @RequestBody AppDto dto) {
        AppDto created = appService.createApp(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppDto> updateApp(
            @PathVariable Integer id,
            @Valid @RequestBody AppDto dto) {
        return ResponseEntity.ok(appService.updateApp(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApp(@PathVariable Integer id) {
        appService.deleteApp(id);
        return ResponseEntity.noContent().build();
    }

    // App Tier endpoints
    @GetMapping("/{appId}/tiers")
    public ResponseEntity<List<AppTierDto>> getTiersByAppId(@PathVariable Integer appId) {
        return ResponseEntity.ok(appService.getTiersByAppId(appId));
    }

    @GetMapping("/tiers/{id}")
    public ResponseEntity<AppTierDto> getTierById(@PathVariable Integer id) {
        return ResponseEntity.ok(appService.getTierById(id));
    }

    @PostMapping("/tiers")
    public ResponseEntity<AppTierDto> createTier(@Valid @RequestBody AppTierDto dto) {
        AppTierDto created = appService.createTier(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/tiers/{id}")
    public ResponseEntity<AppTierDto> updateTier(
            @PathVariable Integer id,
            @Valid @RequestBody AppTierDto dto) {
        return ResponseEntity.ok(appService.updateTier(id, dto));
    }

    @DeleteMapping("/tiers/{id}")
    public ResponseEntity<Void> deleteTier(@PathVariable Integer id) {
        appService.deleteTier(id);
        return ResponseEntity.noContent().build();
    }
}
