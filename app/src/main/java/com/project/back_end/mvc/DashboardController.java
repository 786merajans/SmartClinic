package com.project.back_end.mvc;

import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    private final Service service;

    public DashboardController(Service service) {
        this.service = service;
    }

    // 3. Admin Dashboard
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, Object>> validation = service.validateToken(token, "admin");
        if (validation.getStatusCode() == HttpStatus.OK) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    // 4. Doctor Dashboard
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, Object>> validation = service.validateToken(token, "doctor");
        if (validation.getStatusCode() == HttpStatus.OK) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }
}
