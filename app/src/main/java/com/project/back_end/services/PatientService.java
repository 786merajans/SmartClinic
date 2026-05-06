package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 3. Create Patient
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            System.err.println("Error creating patient: " + e.getMessage());
            return 0;
        }
    }

    // 4. Get Patient Appointments
    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            response.put("status", 1);
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
            response.put("status", 0);
            response.put("message", "Error fetching appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. Filter By Condition
    public ResponseEntity<Map<String, Object>> filterByCondition(Long patientId, String condition) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if (condition.equalsIgnoreCase("future")) {
                status = 0;
            } else if (condition.equalsIgnoreCase("past")) {
                status = 1;
            } else {
                response.put("status", 0);
                response.put("message", "Invalid condition: " + condition);
                return ResponseEntity.badRequest().body(response);
            }
            List<Appointment> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            response.put("status", 1);
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error filtering by condition: " + e.getMessage());
            response.put("status", 0);
            response.put("message", "Error filtering appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. Filter By Doctor
    public ResponseEntity<Map<String, Object>> filterByDoctor(String doctorName, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientId(doctorName, patientId);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            response.put("status", 1);
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error filtering by doctor: " + e.getMessage());
            response.put("status", 0);
            response.put("message", "Error filtering by doctor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 7. Filter By Doctor and Condition
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String doctorName, Long patientId, String condition) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if (condition.equalsIgnoreCase("future")) {
                status = 0;
            } else if (condition.equalsIgnoreCase("past")) {
                status = 1;
            } else {
                response.put("status", 0);
                response.put("message", "Invalid condition: " + condition);
                return ResponseEntity.badRequest().body(response);
            }
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(AppointmentDTO::new)
                    .collect(Collectors.toList());
            response.put("status", 1);
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error filtering by doctor and condition: " + e.getMessage());
            response.put("status", 0);
            response.put("message", "Error filtering appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 8. Get Patient Details
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("status", 0);
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("status", 1);
            response.put("patient", patient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching patient details: " + e.getMessage());
            response.put("status", 0);
            response.put("message", "Error fetching patient details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
