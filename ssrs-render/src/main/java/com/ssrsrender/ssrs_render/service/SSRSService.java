package com.ssrsrender.ssrs_render.service;


import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.ssrsrender.ssrs_render.config.SSRSConfig;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class SSRSService {
    private final SSRSConfig config;

    public SSRSService(SSRSConfig config) {
        this.config = config;
    }

//    public byte[] renderReport(String reportPath, Map<String, String> parameters, String format) throws IOException {
//        // Validate configuration
//        if (config.getBaseUrl() == null || config.getBaseUrl().isEmpty()) {
//            throw new IllegalStateException("SSRS base URL is not configured. Please set ssrs.base.url in application.properties");
//        }
//        
//        String url = buildReportUrl(reportPath, parameters, format);
//        System.out.println("Fetching report from URL: " + url);
//        
//        CloseableHttpClient httpClient;
//        
//        if (config.hasCredentials()) {
//            System.out.println("Using authentication - Type: " + config.getAuth().getType());
//            CredentialsProvider credsProvider = new BasicCredentialsProvider();
//            
//            if (config.isWindowsAuth()) {
//                // Windows/NTLM Authentication
//                String domain = config.getDomain() != null ? config.getDomain() : "";
//                credsProvider.setCredentials(
//                    AuthScope.ANY,
//                    new NTCredentials(config.getUsername(), config.getPassword(), null, domain)
//                );
//            } else {
//                // Basic Authentication
//                credsProvider.setCredentials(
//                    AuthScope.ANY,
//                    new org.apache.http.auth.UsernamePasswordCredentials(
//                        config.getUsername(), 
//                        config.getPassword()
//                    )
//                );
//            }
//            
//            httpClient = HttpClients.custom()
//                    .setDefaultCredentialsProvider(credsProvider)
//                    .build();
//        } else {
//            System.out.println("No authentication configured - attempting anonymous access");
//            httpClient = HttpClients.createDefault();
//        }
//
//        try {
//            HttpGet request = new HttpGet(url);
//            HttpResponse response = httpClient.execute(request);
//            
//            int statusCode = response.getStatusLine().getStatusCode();
//            System.out.println("Response status code: " + statusCode);
//            
//            if (statusCode == 401) {
//                throw new IOException("Authentication failed (401). Please check username and password in application.properties");
//            } else if (statusCode == 404) {
//                throw new IOException("Report not found (404). Please check the report path: " + reportPath);
//            } else if (statusCode >= 400) {
//                throw new IOException("Server error (" + statusCode + "): " + response.getStatusLine().getReasonPhrase());
//            }
//            
//            return EntityUtils.toByteArray(response.getEntity());
//        } finally {
//            httpClient.close();
//        }
//    }
    public byte[] renderReport(String reportPath, Map<String, String> parameters, String format) throws IOException {
        if (config.getBaseUrl() == null || config.getBaseUrl().isEmpty()) {
            throw new IllegalStateException("SSRS base URL is not configured");
        }
        
        String url = buildReportUrl(reportPath, parameters, format);
        System.out.println("Fetching report from URL: " + url);
        
        CloseableHttpClient httpClient;
        
        if (config.hasCredentials()) {
            System.out.println("Using authentication - Username: " + config.getUsername());
            
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            
            // Add NTLM credentials
            credsProvider.setCredentials(
                AuthScope.ANY,
                new NTCredentials(
                    config.getUsername(), 
                    config.getPassword(), 
                    null, 
                    config.getDomain() != null && !config.getDomain().isEmpty() ? config.getDomain() : ""
                )
            );
            
            httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .disableAuthCaching()
                    .build();
        } else {
            httpClient = HttpClients.createDefault();
        }

        try {
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Response status code: " + statusCode);
            
            if (statusCode == 401) {
                String authHeader = response.getFirstHeader("WWW-Authenticate") != null ? 
                    response.getFirstHeader("WWW-Authenticate").getValue() : "None";
                System.err.println("Auth header: " + authHeader);
                throw new IOException("Authentication failed (401). Check credentials. Auth method required: " + authHeader);
            } else if (statusCode == 404) {
                throw new IOException("Report not found (404): " + reportPath);
            } else if (statusCode >= 400) {
                throw new IOException("Server error (" + statusCode + "): " + response.getStatusLine().getReasonPhrase());
            }
            
            byte[] data = EntityUtils.toByteArray(response.getEntity());
            System.out.println("Successfully fetched " + data.length + " bytes");
            return data;
        } finally {
            httpClient.close();
        }
    }

//    private String buildReportUrl(String reportPath, Map<String, String> parameters, String format) {
//        StringBuilder url = new StringBuilder(config.getBaseUrl());
//        
//        // For rendering (download), use ReportServer
//        if (format != null && !format.equalsIgnoreCase("HTML4.0")) {
//            // Remove /Reports and add /ReportServer for rendering
//            String baseUrl = config.getBaseUrl().replace("/Reports", "/ReportServer");
//            url = new StringBuilder(baseUrl);
//            url.append("?");
//            
//            // Add report path
//            if (!reportPath.startsWith("/")) {
//                reportPath = "/" + reportPath;
//            }
//            url.append(reportPath);
//            url.append("&rs:Command=Render");
//            url.append("&rs:Format=").append(format);
//        } else {
//            // For viewing, use Reports path
//            if (!reportPath.startsWith("/")) {
//                reportPath = "/" + reportPath;
//            }
//            url.append(reportPath);
//            
//            if (parameters != null && !parameters.isEmpty()) {
//                boolean first = true;
//                for (Map.Entry<String, String> param : parameters.entrySet()) {
//                    if (!"reportPath".equals(param.getKey()) && !"format".equals(param.getKey())) {
//                        url.append(first ? "?" : "&");
//                        url.append(param.getKey()).append("=")
//                           .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
//                        first = false;
//                    }
//                }
//            }
//        }
//        
//        // Add other parameters
//        if (parameters != null && !parameters.isEmpty()) {
//            for (Map.Entry<String, String> param : parameters.entrySet()) {
//                if (!"reportPath".equals(param.getKey()) && !"format".equals(param.getKey())) {
//                    url.append("&").append(param.getKey()).append("=")
//                       .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
//                }
//            }
//        }
//        
//        return url.toString();
//    }
    
    private String buildReportUrl(String reportPath, Map<String, String> parameters, String format) {
        StringBuilder url = new StringBuilder();
        
        // Clean the report path - remove /report/ prefix if present
        reportPath = reportPath.replaceFirst("^/report/", "/");
        
        if (!reportPath.startsWith("/")) {
            reportPath = "/" + reportPath;
        }
        
        System.out.println("Building URL for report path: " + reportPath);
        
        // For rendering (download), use ReportServer
        if (format != null && !format.equalsIgnoreCase("HTML4.0")) {
            // Use ReportServer for rendering
            String baseUrl = config.getBaseUrl().replace("/Reports", "/ReportServer");
            url.append(baseUrl);
            url.append("?").append(reportPath);
            url.append("&rs:Command=Render");
            url.append("&rs:Format=").append(format);
            
            // Add parameters
            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    if (!"reportPath".equals(param.getKey()) && !"format".equals(param.getKey())) {
                        url.append("&").append(param.getKey()).append("=")
                           .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
                    }
                }
            }
        } else {
            // For HTML viewing
            url.append(config.getBaseUrl());
            url.append("?").append(reportPath);
            
            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    if (!"reportPath".equals(param.getKey()) && !"format".equals(param.getKey())) {
                        url.append("&").append(param.getKey()).append("=")
                           .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
                    }
                }
            }
        }
        
        System.out.println("Final URL: " + url.toString());
        return url.toString();
    }
//    public String getReportViewerUrl(String reportPath, Map<String, String> parameters) {
//        StringBuilder url = new StringBuilder(config.getBaseUrl());
//        
//        if (!reportPath.startsWith("/")) {
//            reportPath = "/" + reportPath;
//        }
//        
//        url.append(reportPath);
//        
//        if (parameters != null && !parameters.isEmpty()) {
//            boolean first = true;
//            for (Map.Entry<String, String> param : parameters.entrySet()) {
//                if (!"reportPath".equals(param.getKey()) && !"format".equals(param.getKey())) {
//                    url.append(first ? "?" : "&");
//                    url.append(param.getKey()).append("=")
//                       .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
//                    first = false;
//                }
//            }
//        }
//        
//        return url.toString();
//    }
    
    public String getReportViewerUrl(String reportPath, Map<String, String> parameters) {
        StringBuilder url = new StringBuilder(config.getBaseUrl());
        
        if (!reportPath.startsWith("/")) {
            reportPath = "/" + reportPath;
        }
        
        // Add ? before report path for SSRS viewer
        url.append("?").append(reportPath);
        
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                if (!"reportPath".equals(param.getKey()) && !"format".equals(param.getKey())) {
                    url.append("&").append(param.getKey()).append("=")
                       .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
                }
            }
        }
        
        return url.toString();
    }
   
    public String getBaseUrl() {
        return config.getBaseUrl();
    }
}
