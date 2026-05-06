package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // 3. Get Appointments
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {

        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        String email = service.getDoctorEmailFromToken(token);
        LocalDate localDate = LocalDate.parse(date);

        return ResponseEntity.ok(Map.of(
                "appointments",
                appointmentService.getAppointments(
                        service.getDoctorIdByEmail(email),
                        localDate.atStartOfDay(),
                        localDate.plusDays(1).atStartOfDay(),
                        patientName.equals("null") ? null : patientName)
        ));
    }

    // 4. Book Appointment
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();

        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        int valid = service.validateAppointment(appointment.getDoctor().getId(), appointment.getAppointmentTime());
        if (valid == -1) {
            response.put("status", 0);
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (valid == 0) {
            response.put("status", 0);
            response.put("message", "Doctor is not available at the requested time");
            return ResponseEntity.badRequest().body(response);
        }

        int result = appointmentService.bookAppointment(appointment);
        if (result == 1) {
            response.put("status", 1);
            response.put("message", "Appointment booked successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("status", 0);
            response.put("message", "Failed to book appointment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. Update Appointment
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {

        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        Long patientId = service.getPatientIdByToken(token);
        return ResponseEntity.ok(appointmentService.updateAppointment(appointment, patientId));
    }

    // 6. Cancel Appointment
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<Map<String, Object>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) return tokenValidation;

        Long patientId = service.getPatientIdByToken(token);
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, patientId));
    }
}
