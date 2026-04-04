package com.sky.subscription.controller;

import com.sky.subscription.dto.PackageDto;
import com.sky.subscription.dto.PackageTierDto;
import com.sky.subscription.service.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    // Package endpoints
    @GetMapping
    public ResponseEntity<List<PackageDto>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageDto> getPackageById(@PathVariable Integer id) {
        return ResponseEntity.ok(packageService.getPackageById(id));
    }

    @PostMapping
    public ResponseEntity<PackageDto> createPackage(@Valid @RequestBody PackageDto dto) {
        PackageDto created = packageService.createPackage(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageDto> updatePackage(
            @PathVariable Integer id,
            @Valid @RequestBody PackageDto dto) {
        return ResponseEntity.ok(packageService.updatePackage(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Integer id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

    // Package Tier endpoints
    @GetMapping("/{packageId}/tiers")
    public ResponseEntity<List<PackageTierDto>> getTiersByPackageId(@PathVariable Integer packageId) {
        return ResponseEntity.ok(packageService.getTiersByPackageId(packageId));
    }

    @GetMapping("/tiers/{id}")
    public ResponseEntity<PackageTierDto> getTierById(@PathVariable Integer id) {
        return ResponseEntity.ok(packageService.getTierById(id));
    }

    @PostMapping("/tiers")
    public ResponseEntity<PackageTierDto> createTier(@Valid @RequestBody PackageTierDto dto) {
        PackageTierDto created = packageService.createTier(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/tiers/{id}")
    public ResponseEntity<PackageTierDto> updateTier(
            @PathVariable Integer id,
            @Valid @RequestBody PackageTierDto dto) {
        return ResponseEntity.ok(packageService.updateTier(id, dto));
    }

    @DeleteMapping("/tiers/{id}")
    public ResponseEntity<Void> deleteTier(@PathVariable Integer id) {
        packageService.deleteTier(id);
        return ResponseEntity.noContent().build();
    }
}
