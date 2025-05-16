package com.maiia.pro.mapper;

import com.maiia.pro.dto.PractitionerDTO;
import com.maiia.pro.entity.Practitioner;

public class PractitionerMapper {

    public static PractitionerDTO toDTO(Practitioner practitioner) {
        return PractitionerDTO.builder()
                .id(practitioner.getId())
                .firstName(practitioner.getFirstName())
                .lastName(practitioner.getLastName())
                .speciality(practitioner.getSpeciality())
                .build();
    }

    public static Practitioner toEntity(PractitionerDTO practitionerDTO) {
        return Practitioner.builder()
                .id(practitionerDTO.getId())
                .firstName(practitionerDTO.getFirstName())
                .lastName(practitionerDTO.getLastName())
                .speciality(practitionerDTO.getSpeciality())
                .build();
    }
}
