package com.maiia.pro.mapper;

import com.maiia.pro.dto.AvailabilityDTO;
import com.maiia.pro.entity.Availability;

public class AvailabilityMapper {

    public static AvailabilityDTO toDTO(Availability availability) {
        return AvailabilityDTO.builder()
                .id(availability.getId())
                .practitionerId(availability.getId())
                .startDate(availability.getStartDate())
                .endDate(availability.getEndDate())
                .build();
    }

    public static Availability toEntity(AvailabilityDTO availabilityDTO) {
        return Availability.builder()
                .id(availabilityDTO.getId())
                .practitionerId(availabilityDTO.getPractitionerId())
                .startDate(availabilityDTO.getStartDate())
                .endDate(availabilityDTO.getEndDate())
                .build();
    }
}
