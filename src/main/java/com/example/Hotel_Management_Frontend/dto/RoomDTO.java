package com.example.Hotel_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomDTO {
    private Integer roomId;
    private Integer roomNumber;
    private Boolean isAvailable;
    private Integer hotelId;
    private Integer roomTypeId;
    private RoomTypeDTO1 roomType;
    private List<AmenityDTO> amenities;
}
