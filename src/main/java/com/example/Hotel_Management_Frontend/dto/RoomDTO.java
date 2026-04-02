package com.example.Hotel_Management_Frontend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoomDTO {
    private Integer roomId;
    private Integer roomNumber;
    private Boolean isAvailable;
    private Integer hotelId;
    private RoomTypeDTO1 roomType;
    private List<AmenityDTO> amenities;
}
