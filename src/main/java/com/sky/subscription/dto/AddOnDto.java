package com.sky.subscription.dto;

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
public class AddOnDto {
    
    private Integer id;
    
    @NotBlank(message = "Add-on name is required")
    @Size(max = 100, message = "Add-on name must be at most 100 characters")
    private String addonName;
    
    @NotNull(message = "Monthly cost is required")
    @Positive(message = "Monthly cost must be positive")
    private BigDecimal costMonthly;
    
    private Boolean included;
}
