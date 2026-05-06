package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 4. Get Doctor Availability
    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor == null) return Collections.emptyList();

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            List<String> bookedSlots = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end)
                    .stream()
                    .map(a -> a.getAppointmentTime().toLocalTime().toString())
                    .collect(Collectors.toList());

            return doctor.getAvailableTimes().stream()
                    .filter(slot -> !bookedSlots.contains(slot))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 5. Save Doctor
    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) return -1;
            doctor.setPassword(doctor.getPassword());
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 6. Update Doctor
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) return -1;
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 7. Get Doctors
    @Transactional
    public List<Doctor> getDoctors() {
        try {
            List<Doctor> doctors = doctorRepository.findAll();
            doctors.forEach(d -> d.getAvailableTimes().size());
            return doctors;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 8. Delete Doctor
    public int deleteDoctor(Long doctorId) {
        try {
            if (!doctorRepository.existsById(doctorId)) return -1;
            appointmentRepository.deleteAllByDoctorId(doctorId);
            doctorRepository.deleteById(doctorId);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 9. Validate Doctor
    public Map<String, Object> validateDoctor(String email, String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            Doctor doctor = doctorRepository.findByEmail(email);
            if (doctor == null) {
                response.put("status", 0);
                response.put("message", "Doctor not found");
                return response;
            }
            if (!doctor.getPassword().equals(password)) {
                response.put("status", 0);
                response.put("message", "Invalid password");
                return response;
            }
            String token = tokenService.generateToken(email);
            response.put("status", 1);
            response.put("token", token);
            response.put("doctorId", doctor.getId());
        } catch (Exception e) {
            response.put("status", 0);
            response.put("message", "Error during validation: " + e.getMessage());
        }
        return response;
    }

    // 10. Find Doctor By Name
    @Transactional
    public List<Doctor> findDoctorByName(String name) {
        try {
            List<Doctor> doctors = doctorRepository.findByNameLike(name);
            doctors.forEach(d -> d.getAvailableTimes().size());
            return doctors;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 11. Filter Doctors By Name, Specialty, and Time
    @Transactional
    public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
        try {
            List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
            return filterDoctorByTime(doctors, time);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 12. Filter Doctor By Time (helper)
    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String time) {
        return doctors.stream().filter(doctor -> {
            List<String> slots = doctor.getAvailableTimes();
            if (slots == null || slots.isEmpty()) return false;
            return slots.stream().anyMatch(slot -> {
                try {
                    LocalTime localTime = LocalTime.parse(slot.split("-")[0].trim());
                    if (time.equalsIgnoreCase("AM")) return localTime.getHour() < 12;
                    if (time.equalsIgnoreCase("PM")) return localTime.getHour() >= 12;
                } catch (Exception ignored) {}
                return false;
            });
        }).collect(Collectors.toList());
    }

    // 13. Filter Doctor By Name and Time
    @Transactional
    public List<Doctor> filterDoctorByNameAndTime(String name, String time) {
        try {
            List<Doctor> doctors = doctorRepository.findByNameLike(name);
            return filterDoctorByTime(doctors, time);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 14. Filter Doctor By Name and Specialty
    @Transactional
    public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
        try {
            return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 15. Filter Doctor By Time and Specialty
    @Transactional
    public List<Doctor> filterDoctorByTimeAndSpecility(String specialty, String time) {
        try {
            List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            return filterDoctorByTime(doctors, time);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 16. Filter Doctor By Specialty
    @Transactional
    public List<Doctor> filterDoctorBySpecility(String specialty) {
        try {
            return doctorRepository.findBySpecialtyIgnoreCase(specialty);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 17. Filter Doctors By Time
    @Transactional
    public List<Doctor> filterDoctorsByTime(String time) {
        try {
            List<Doctor> doctors = doctorRepository.findAll();
            return filterDoctorByTime(doctors, time);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
