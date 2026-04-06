package com.sky.subscription.dto;

import com.sky.subscription.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    
    private Integer id;
    
    @NotNull(message = "Subscription ID is required")
    private Integer subscriptionId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    private LocalDate paidDate;
    
    @NotNull(message = "Status is required")
    private PaymentStatus status;
}
