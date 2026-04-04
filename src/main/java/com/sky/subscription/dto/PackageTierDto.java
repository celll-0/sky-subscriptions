package com.sky.subscription.dto;

import com.sky.subscription.entity.PaymentCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageTierDto {
    
    private Integer packageTierId;
    
    @NotNull(message = "Package ID is required")
    private Integer packageId;
    
    private String packageName;
    
    @NotBlank(message = "Tier name is required")
    @Size(max = 100, message = "Tier name must be at most 100 characters")
    private String tierName;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Payment cycle is required")
    private PaymentCycle paymentCycle;
}
