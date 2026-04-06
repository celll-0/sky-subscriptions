package com.sky.subscription.repository;

import com.sky.subscription.entity.AppTier;
import com.sky.subscription.entity.PaymentCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppTierRepository extends JpaRepository<AppTier, Integer> {
    
    List<AppTier> findByAppId(Integer appId);
    
    List<AppTier> findByPaymentCycle(PaymentCycle paymentCycle);
}
