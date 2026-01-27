package com.crm.smart_CRM.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crm.smart_CRM.Enum.ResourceStatus;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.ResourceCategory;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    // Find by category
    List<Resource> findByCategory(ResourceCategory category);
    
    // Find by category ID
    List<Resource> findByCategoryId(Long categoryId);
    
    // Find by status
    List<Resource> findByStatus(ResourceStatus status);
    
    // Find by name (case-insensitive, contains)
    List<Resource> findByNameContainingIgnoreCase(String name);
    
    // Find by location
    List<Resource> findByLocation(String location);
    
    // Find by location (contains)
    List<Resource> findByLocationContainingIgnoreCase(String location);
    
    // Find by category and status
    @Query("SELECT r FROM Resource r WHERE r.category.id = :categoryId AND r.status = :status")
    List<Resource> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, 
                                              @Param("status") ResourceStatus status);
    
    // Find by minimum capacity
    @Query("SELECT r FROM Resource r WHERE r.capacity >= :minCapacity")
    List<Resource> findByMinCapacity(@Param("minCapacity") Integer minCapacity);
    
    // Find by capacity range
    @Query("SELECT r FROM Resource r WHERE r.capacity BETWEEN :minCapacity AND :maxCapacity")
    List<Resource> findByCapacityRange(@Param("minCapacity") Integer minCapacity, 
                                        @Param("maxCapacity") Integer maxCapacity);
    
    // Count by status
    Long countByStatus(ResourceStatus status);
    
    // Find available resources
    List<Resource> findByStatusOrderByNameAsc(ResourceStatus status);
}
