package com.sky.subscription.repository;

import com.sky.subscription.entity.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddOnRepository extends JpaRepository<AddOn, Integer> {
    
    Optional<AddOn> findByAddonName(String addonName);
}
