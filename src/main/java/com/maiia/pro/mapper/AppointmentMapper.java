package com.maiia.pro.mapper;

import com.maiia.pro.dto.AppointmentDTO;
import com.maiia.pro.dto.PractitionerDTO;
import com.maiia.pro.entity.Appointment;
import com.maiia.pro.entity.Practitioner;

public class AppointmentMapper {

    public static AppointmentDTO toDTO(Appointment appointment) {
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .practitionerId(appointment.getPractitionerId())
                .startDate(appointment.getStartDate())
                .endDate(appointment.getEndDate())
                .build();
    }

    public static Appointment toEntity(AppointmentDTO appointmentDTO) {
        return Appointment.builder()
                .id(appointmentDTO.getId())
                .patientId(appointmentDTO.getPatientId())
                .practitionerId(appointmentDTO.getPractitionerId())
                .startDate(appointmentDTO.getStartDate())
                .endDate(appointmentDTO.getEndDate())
                .build();
    }
}
