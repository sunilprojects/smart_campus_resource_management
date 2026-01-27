package com.crm.smart_CRM.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.smart_CRM.model.ResourceCategory;

import java.util.Optional;

@Repository
public interface ResourceCategoryRepository extends JpaRepository<ResourceCategory, Long> {
    
    // Find by name
    Optional<ResourceCategory> findByName(String name);
    
    // Check if name exists
    boolean existsByName(String name);
}