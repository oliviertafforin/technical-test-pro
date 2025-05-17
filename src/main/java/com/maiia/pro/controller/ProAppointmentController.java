package com.maiia.pro.controller;

import com.maiia.pro.dto.AppointmentDTO;
import com.maiia.pro.entity.Appointment;
import com.maiia.pro.mapper.AppointmentMapper;
import com.maiia.pro.service.ProAppointmentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProAppointmentController {
    @Autowired
    private ProAppointmentService proAppointmentService;

    @ApiOperation(value = "Get appointments by practitionerId")
    @GetMapping("/{practitionerId}")
    public List<AppointmentDTO> getAppointmentsByPractitioner(@PathVariable final Integer practitionerId) {
        return proAppointmentService.findByPractitionerId(practitionerId)
                .stream().map(AppointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Get all appointments")
    @GetMapping
    public List<AppointmentDTO> getAppointments() {
        return proAppointmentService.findAll().stream()
                .map(AppointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Create an appointment")
    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        Appointment appointment = proAppointmentService.createAppointment(AppointmentMapper.toEntity(appointmentDTO));
        AppointmentDTO appointmentCreated = AppointmentMapper.toDTO(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentCreated);
    }
}
