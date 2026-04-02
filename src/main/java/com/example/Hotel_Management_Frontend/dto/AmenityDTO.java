package com.example.Hotel_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmenityDTO {
    private Integer amenityId;
    private String name;
    private String description;
}
