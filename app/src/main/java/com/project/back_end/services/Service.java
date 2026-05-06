package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // 3. Validate Token
    public ResponseEntity<Map<String, Object>> validateToken(String token, String role) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean valid = tokenService.validateToken(token, role);
            if (!valid) {
                response.put("status", 0);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("status", 1);
            response.put("message", "Token is valid");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Token validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // 4. Validate Admin
    public ResponseEntity<Map<String, Object>> validateAdmin(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(username);
            if (admin == null) {
                response.put("status", 0);
                response.put("message", "Admin not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            if (!admin.getPassword().equals(password)) {
                response.put("status", 0);
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(admin.getUsername());
            response.put("status", 1);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. Filter Doctor
    public ResponseEntity<Map<String, Object>> filterDoctor(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Doctor> doctors;
            boolean hasName = name != null && !name.isEmpty();
            boolean hasSpecialty = specialty != null && !specialty.isEmpty();
            boolean hasTime = time != null && !time.isEmpty();

            if (hasName && hasSpecialty && hasTime) {
                doctors = doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
            } else if (hasName && hasSpecialty) {
                doctors = doctorService.filterDoctorByNameAndSpecility(name, specialty);
            } else if (hasName && hasTime) {
                doctors = doctorService.filterDoctorByNameAndTime(name, time);
            } else if (hasSpecialty && hasTime) {
                doctors = doctorService.filterDoctorByTimeAndSpecility(specialty, time);
            } else if (hasName) {
                doctors = doctorService.findDoctorByName(name);
            } else if (hasSpecialty) {
                doctors = doctorService.filterDoctorBySpecility(specialty);
            } else if (hasTime) {
                doctors = doctorService.filterDoctorsByTime(time);
            } else {
                doctors = doctorService.getDoctors();
            }

            response.put("status", 1);
            response.put("doctors", doctors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Error filtering doctors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. Validate Appointment
    public int validateAppointment(Long doctorId, LocalDateTime appointmentTime) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor == null) return -1;

            LocalDate date = appointmentTime.toLocalDate();
            List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

            LocalTime requestedTime = appointmentTime.toLocalTime();
            for (String slot : availableSlots) {
                LocalTime slotStart = LocalTime.parse(slot.split("-")[0].trim());
                if (slotStart.equals(requestedTime)) return 1;
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // 7. Validate Patient
    public boolean validatePatient(String email, String phone) {
        try {
            return patientRepository.findByEmailOrPhone(email, phone) == null;
        } catch (Exception e) {
            return false;
        }
    }

    // 8. Validate Patient Login
    public ResponseEntity<Map<String, Object>> validatePatientLogin(String email, String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            var patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("status", 0);
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            if (!patient.getPassword().equals(password)) {
                response.put("status", 0);
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(email);
            response.put("status", 1);
            response.put("token", token);
            response.put("patientId", patient.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 9. Filter Patient
    public ResponseEntity<Map<String, Object>> filterPatient(String token, String condition, String doctorName) {
        try {
            String email = tokenService.extractEmail(token);
            var patient = patientRepository.findByEmail(email);
            if (patient == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", 0);
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Long patientId = patient.getId();
            boolean hasCondition = condition != null && !condition.isEmpty();
            boolean hasDoctorName = doctorName != null && !doctorName.isEmpty();

            if (hasCondition && hasDoctorName) {
                return patientService.filterByDoctorAndCondition(doctorName, patientId, condition);
            } else if (hasCondition) {
                return patientService.filterByCondition(patientId, condition);
            } else if (hasDoctorName) {
                return patientService.filterByDoctor(doctorName, patientId);
            } else {
                return patientService.getPatientAppointment(patientId);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 0);
            response.put("message", "Error filtering patient appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
