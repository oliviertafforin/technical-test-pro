package com.maiia.pro.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class PractitionerDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String speciality;
}
