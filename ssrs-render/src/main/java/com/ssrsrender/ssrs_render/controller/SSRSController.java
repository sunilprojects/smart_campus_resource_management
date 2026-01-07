package com.ssrsrender.ssrs_render.controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ssrsrender.ssrs_render.service.SSRSService;

import java.util.Map;

@Controller
@RequestMapping("/reports")
public class SSRSController {
    private final SSRSService ssrsService;

    public SSRSController(SSRSService ssrsService) {
        this.ssrsService = ssrsService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("baseUrl", ssrsService.getBaseUrl());
        return "index";
    }
    
    @GetMapping("/config-check")
    @ResponseBody
    public ResponseEntity<String> checkConfig() {
        StringBuilder info = new StringBuilder();
        info.append("SSRS Configuration Status:\n\n");
        info.append("Base URL: ").append(ssrsService.getBaseUrl() != null ? ssrsService.getBaseUrl() : "NOT SET").append("\n");
        info.append("Has Credentials: ").append(ssrsService.getBaseUrl() != null ? "Yes" : "No").append("\n\n");
        info.append("If any value shows 'NOT SET', please configure it in application.properties");
        
        return ResponseEntity.ok(info.toString());
    }

    @GetMapping("/viewer")
    public String showReportViewer(@RequestParam(required = false) String reportPath,
                                   @RequestParam(required = false) Map<String, String> params,
                                   Model model) {
        if (reportPath == null || reportPath.isEmpty()) {
            model.addAttribute("error", "Report path is required");
            model.addAttribute("baseUrl", ssrsService.getBaseUrl());
            return "index";
        }
        
        // Build direct SSRS URL for iframe
        String reportUrl = ssrsService.getReportViewerUrl(reportPath, params);
        model.addAttribute("reportUrl", reportUrl);
        model.addAttribute("reportPath", reportPath);
        model.addAttribute("ssrsBaseUrl", ssrsService.getBaseUrl());
        return "report-viewer";
    }
    
    @GetMapping("/embed")
    public String embedReport(@RequestParam(required = false) String reportPath,
                             @RequestParam(required = false) Map<String, String> params,
                             Model model) {
        if (reportPath == null || reportPath.isEmpty()) {
            model.addAttribute("error", "Report path is required");
            model.addAttribute("baseUrl", ssrsService.getBaseUrl());
            return "index";
        }
        
        model.addAttribute("reportPath", reportPath);
        model.addAttribute("params", params);
        return "report-embed";
    }
    
    @GetMapping("/proxy")
    @ResponseBody
    public ResponseEntity<byte[]> proxyReport(@RequestParam String reportPath,
                                              @RequestParam(required = false) Map<String, String> params) {
        try {
            System.out.println("Proxying report: " + reportPath);
            System.out.println("Parameters: " + params);
            
            byte[] reportData = ssrsService.renderReport(reportPath, params, "HTML4.0");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error details: " + e.getMessage());
            
            String errorHtml = "<html><body style='font-family: Arial; padding: 40px;'>" +
                             "<h2 style='color: #c53030;'>❌ Error Loading Report</h2>" +
                             "<div style='background: #fed7d7; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
                             "<strong>Report Path:</strong> " + reportPath + "<br><br>" +
                             "<strong>Error:</strong> " + e.getMessage() + "<br><br>" +
                             "<strong>Error Type:</strong> " + e.getClass().getSimpleName() +
                             "</div>" +
                             "<p><strong>Common Solutions:</strong></p>" +
                             "<ul style='line-height: 1.8;'>" +
                             "<li>Check if the report path is correct (should be like: /FolderName/ReportName)</li>" +
                             "<li>Verify your SSRS credentials in application.properties</li>" +
                             "<li>Make sure the SSRS server is accessible</li>" +
                             "<li>Check if you have permission to access this report</li>" +
                             "</ul>" +
                             "<a href='/reports' style='display: inline-block; margin-top: 20px; padding: 10px 20px; background: #667eea; color: white; text-decoration: none; border-radius: 5px;'>← Back to Home</a>" +
                             "</body></html>";
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorHtml.getBytes());
        }
    }

    @GetMapping("/render")
    public ResponseEntity<byte[]> renderReport(
            @RequestParam(required = false) String reportPath,
            @RequestParam(defaultValue = "PDF") String format,
            @RequestParam(required = false) Map<String, String> params) {
        
        if (reportPath == null || reportPath.isEmpty()) {
            return ResponseEntity.badRequest().body("Report path is required".getBytes());
        }
        
        try {
            System.out.println("Rendering report: " + reportPath + " in format: " + format);
            byte[] reportData = ssrsService.renderReport(reportPath, params, format);
            
            System.out.println("Report data size: " + reportData.length + " bytes");
            
            HttpHeaders headers = new HttpHeaders();
            MediaType mediaType = getMediaType(format);
            headers.setContentType(mediaType);
            headers.setContentLength(reportData.length);
            
            String filename = "report." + format.toLowerCase();
            if (format.equalsIgnoreCase("EXCEL")) {
                filename = "report.xls";
            }
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error rendering report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error rendering report: " + e.getMessage()).getBytes());
        }
    }

    private MediaType getMediaType(String format) {
        switch (format.toUpperCase()) {
            case "PDF": return MediaType.APPLICATION_PDF;
            case "EXCEL": 
            case "EXCELOPENXML": 
                return MediaType.parseMediaType("application/vnd.ms-excel");
            case "WORD": 
            case "WORDOPENXML":
                return MediaType.parseMediaType("application/msword");
            case "IMAGE": 
            case "TIFF":
                return MediaType.IMAGE_PNG;
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}