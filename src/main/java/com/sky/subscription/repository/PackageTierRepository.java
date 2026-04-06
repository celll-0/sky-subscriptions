package com.sky.subscription.repository;

import com.sky.subscription.entity.PackageTier;
import com.sky.subscription.entity.PaymentCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageTierRepository extends JpaRepository<PackageTier, Integer> {
    
    List<PackageTier> findByPkgId(Integer packageId);
    
    List<PackageTier> findByPaymentCycle(PaymentCycle paymentCycle);
}
