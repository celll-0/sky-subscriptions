package com.sky.subscription.dto;

import com.sky.subscription.entity.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    
    private Integer subscriptionId;
    
    @NotNull(message = "Customer ID is required")
    private Integer customerId;
    
    private String customerName;
    
    @NotNull(message = "Package tier ID is required")
    private Integer packageTierId;
    
    private String packageTierName;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull(message = "Status is required")
    private SubscriptionStatus status;
    
    private LocalDateTime updatedAt;
    
    private List<Integer> addOnIds;
    
    private List<Integer> appTierIds;
}
