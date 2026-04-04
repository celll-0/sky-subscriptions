package com.sky.subscription.repository;

import com.sky.subscription.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Integer> {
    
    Optional<Package> findByPackageName(String packageName);
}
