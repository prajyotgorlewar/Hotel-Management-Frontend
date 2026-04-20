package com.example.Hotel_Management_Frontend.dto;

import lombok.Data;
import java.util.List;

@Data
public class HotelDTO {
    private Integer hotelId;
    private String name;
    private String location;
    private String description;
    private List<AmenityDTO> amenities;
}