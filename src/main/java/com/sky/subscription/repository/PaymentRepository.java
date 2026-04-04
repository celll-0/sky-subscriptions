package com.sky.subscription.repository;

import com.sky.subscription.entity.Payment;
import com.sky.subscription.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    List<Payment> findBySubscriptionSubscriptionId(Integer subscriptionId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Payment> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);
}
