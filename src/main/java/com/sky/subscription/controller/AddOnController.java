package com.sky.subscription.controller;

import com.sky.subscription.dto.AddOnDto;
import com.sky.subscription.service.AddOnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddOnController {

    private final AddOnService addOnService;

    @GetMapping
    public ResponseEntity<List<AddOnDto>> getAllAddOns() {
        return ResponseEntity.ok(addOnService.getAllAddOns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddOnDto> getAddOnById(@PathVariable Integer id) {
        return ResponseEntity.ok(addOnService.getAddOnById(id));
    }

    @PostMapping
    public ResponseEntity<AddOnDto> createAddOn(@Valid @RequestBody AddOnDto dto) {
        AddOnDto created = addOnService.createAddOn(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddOnDto> updateAddOn(
            @PathVariable Integer id,
            @Valid @RequestBody AddOnDto dto) {
        return ResponseEntity.ok(addOnService.updateAddOn(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddOn(@PathVariable Integer id) {
        addOnService.deleteAddOn(id);
        return ResponseEntity.noContent().build();
    }
}
