package com.crm.smart_CRM.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.smart_CRM.model.SystemConfig;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    // Find by config key
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    // Check if config key exists
    boolean existsByConfigKey(String configKey);
    
    // Delete by config key
    void deleteByConfigKey(String configKey);
}
