package com.ssrsrender.ssrs_render.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSRSConfig {
    
    @Value("${ssrs.base-url}")
    private String baseUrl;
    
    @Value("${ssrs.username}")
    private String username;
    
    @Value("${ssrs.password}")
    private String password;
    
    @Value("${ssrs.domain:}")
    private String domain;
    
    @Value("${ssrs.auth.type:BASIC}")
    private String authType;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    
    public boolean hasCredentials() {
        return username != null && !username.isEmpty() && 
               password != null && !password.isEmpty();
    }
    
    public boolean isWindowsAuth() {
        return "WINDOWS".equalsIgnoreCase(authType);
    }
    
    public Auth getAuth() {
        Auth auth = new Auth();
        auth.setType(authType);
        return auth;
    }
    
    public static class Auth {
        private String type = "BASIC";
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
