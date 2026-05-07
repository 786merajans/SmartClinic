package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TokenService tokenService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.tokenService = tokenService;
    }

    // 4. Book Appointment
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 5. Update Appointment
    @Transactional
    public Map<String, Object> updateAppointment(Appointment appointment, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Appointment existing = appointmentRepository.findById(appointment.getId()).orElse(null);
            if (existing == null) {
                response.put("status", 0);
                response.put("message", "Appointment not found");
                return response;
            }
            if (!existing.getPatient().getId().equals(patientId)) {
                response.put("status", 0);
                response.put("message", "Unauthorized: Patient ID mismatch");
                return response;
            }
            LocalDateTime newTime = appointment.getAppointmentTime();
            boolean doctorAvailable = existing.getDoctor().getAvailableTimes()
                    .contains(newTime.toLocalTime().toString());
            if (!doctorAvailable) {
                response.put("status", 0);
                response.put("message", "Doctor is not available at the requested time");
                return response;
            }
            existing.setAppointmentTime(newTime);
            existing.setStatus(appointment.getStatus());
            appointmentRepository.save(existing);
            response.put("status", 1);
            response.put("message", "Appointment updated successfully");
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Error updating appointment: " + e.getMessage());
        }
        return response;
    }

    // 6. Cancel Appointment
    @Transactional
    public Map<String, Object> cancelAppointment(Long appointmentId, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Appointment existing = appointmentRepository.findById(appointmentId).orElse(null);
            if (existing == null) {
                response.put("status", 0);
                response.put("message", "Appointment not found");
                return response;
            }
            if (!existing.getPatient().getId().equals(patientId)) {
                response.put("status", 0);
                response.put("message", "Unauthorized: Patient ID mismatch");
                return response;
            }
            appointmentRepository.delete(existing);
            response.put("status", 1);
            response.put("message", "Appointment cancelled successfully");
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Error cancelling appointment: " + e.getMessage());
        }
        return response;
    }

    // 7. Get Appointments
    @Transactional
    public List<Appointment> getAppointments(Long doctorId, LocalDateTime start, LocalDateTime end, String patientName) {
        try {
            if (patientName != null && !patientName.isEmpty()) {
                return appointmentRepository
                        .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                doctorId, patientName, start, end);
            }
            return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 8. Change Status
    @Transactional
    public Map<String, Object> changeStatus(int status, long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            appointmentRepository.updateStatus(status, appointmentId);
            response.put("status", 1);
            response.put("message", "Appointment status updated successfully");
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Error updating status: " + e.getMessage());
        }
        return response;
    }
}
