package com.maiia.pro.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class PatientDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
}
