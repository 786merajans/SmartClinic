package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // 3. Get Doctor Availability
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, user);
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        LocalDate localDate = LocalDate.parse(date);
        response.put("status", 1);
        response.put("availability", doctorService.getDoctorAvailability(doctorId, localDate));
        return ResponseEntity.ok(response);
    }

    // 4. Get All Doctors
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 1);
        response.put("doctors", doctorService.getDoctors());
        return ResponseEntity.ok(response);
    }

    // 5. Save Doctor
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> saveDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        int result = doctorService.saveDoctor(doctor);
        if (result == -1) {
            response.put("status", 0);
            response.put("message", "Doctor with this email already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else if (result == 1) {
            response.put("status", 1);
            response.put("message", "Doctor registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("status", 0);
            response.put("message", "Failed to register doctor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. Doctor Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> doctorLogin(@Valid @RequestBody Login login) {
        return doctorService.validateDoctor(login.getEmail(), login.getPassword());
    }

    // 7. Update Doctor
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        int result = doctorService.updateDoctor(doctor);
        if (result == -1) {
            response.put("status", 0);
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (result == 1) {
            response.put("status", 1);
            response.put("message", "Doctor updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", 0);
            response.put("message", "Failed to update doctor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 8. Delete Doctor
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        int result = doctorService.deleteDoctor(id);
        if (result == -1) {
            response.put("status", 0);
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (result == 1) {
            response.put("status", 1);
            response.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", 0);
            response.put("message", "Failed to delete doctor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 9. Filter Doctors
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {

        return service.filterDoctor(
                name.equals("null") ? null : name,
                speciality.equals("null") ? null : speciality,
                time.equals("null") ? null : time);
    }
}
