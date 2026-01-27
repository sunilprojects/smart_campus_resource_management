package com.crm.smart_CRM.controller;


import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crm.smart_CRM.dto.response.ApiResponse;
import com.crm.smart_CRM.model.SystemConfig;
import com.crm.smart_CRM.repository.SystemConfigRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SystemConfigController {
    
    private final SystemConfigRepository systemConfigRepository;
    
    /**
     * Get all system configurations (Admin only)
     * GET /api/config
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getAllConfigurations() {
        log.info("Get all system configurations request");
        
        List<SystemConfig> configs = systemConfigRepository.findAll();
        
        return ResponseEntity.ok(ApiResponse.success("Configurations retrieved successfully", configs));
    }
    
    /**
     * Get configuration by key
     * GET /api/config/{key}
     */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfig>> getConfigurationByKey(@PathVariable String key) {
        log.info("Get configuration by key request: {}", key);
        
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElse(null);
        
        if (config == null) {
            return ResponseEntity.ok(ApiResponse.error("Configuration not found"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Configuration retrieved successfully", config));
    }
    
    /**
     * Update configuration (Admin only)
     * PUT /api/config/{key}
     */
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfig>> updateConfiguration(
            @PathVariable String key,
            @RequestBody Map<String, String> updateRequest) {
        
        log.info("Update configuration request for key: {}", key);
        
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Configuration not found"));
        
        config.setConfigValue(updateRequest.get("value"));
        
        if (updateRequest.containsKey("description")) {
            config.setDescription(updateRequest.get("description"));
        }
        
        SystemConfig updatedConfig = systemConfigRepository.save(config);
        
        return ResponseEntity.ok(ApiResponse.success("Configuration updated successfully", updatedConfig));
    }
}