package com.sky.subscription.repository;

import com.sky.subscription.entity.PackageIncludedApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageIncludedAppRepository extends JpaRepository<PackageIncludedApp, Integer> {
    
    List<PackageIncludedApp> findByPkgId(Integer packageId);
    
    List<PackageIncludedApp> findByAppTierId(Integer appTierId);
}
