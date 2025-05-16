package com.maiia.pro.mapper;

import com.maiia.pro.dto.PatientDTO;
import com.maiia.pro.entity.Patient;

public class PatientMapper {

    public static PatientDTO toDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .build();
    }

    public static Patient toEntity(PatientDTO patientDTO) {
        return Patient.builder()
                .id(patientDTO.getId())
                .firstName(patientDTO.getFirstName())
                .lastName(patientDTO.getLastName())
                .birthDate(patientDTO.getBirthDate())
                .build();
    }
}
