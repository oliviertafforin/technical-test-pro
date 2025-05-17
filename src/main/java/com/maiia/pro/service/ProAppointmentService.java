package com.maiia.pro.service;

import com.maiia.pro.entity.Appointment;
import com.maiia.pro.exception.ResourceNotFoundException;
import com.maiia.pro.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProAppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(ProAppointmentService.class);

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public ProAppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment find(String appointmentId) {
        if (appointmentId == null) {
            logger.error("Cannot find appointment : appointment ID is null");
            throw new IllegalArgumentException("Appointment ID shouldn't be null");
        }
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> findByPractitionerId(Integer practitionerId) {
        if (practitionerId == null) {
            logger.error("Cannot find appointments: Practitioner ID is null");
            throw new IllegalArgumentException("Practitioner ID shouldn't be null");
        }
        return appointmentRepository.findByPractitionerId(practitionerId);
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Appointment saved with id:{}", savedAppointment.getId());
        return savedAppointment;
    }
}
