package com.maiia.pro.mapper;

import com.maiia.pro.dto.TimeSlotDTO;
import com.maiia.pro.entity.TimeSlot;

public class TimeSlotMapper {

    public static TimeSlotDTO toDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .practitionerId(timeSlot.getId())
                .startDate(timeSlot.getStartDate())
                .endDate(timeSlot.getEndDate())
                .build();
    }

    public static TimeSlot toEntity(TimeSlotDTO timeSlotDTO) {
        return TimeSlot.builder()
                .id(timeSlotDTO.getId())
                .practitionerId(timeSlotDTO.getPractitionerId())
                .startDate(timeSlotDTO.getStartDate())
                .endDate(timeSlotDTO.getEndDate())
                .build();
    }
}
