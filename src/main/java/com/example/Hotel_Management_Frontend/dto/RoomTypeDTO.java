package com.example.Hotel_Management_Frontend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomTypeDTO {

    @JsonProperty("room_type_id")
    private int id;

    private String type_name;
    private Integer max_occupancy;
    

    @JsonProperty("guest_phone")
    private BigDecimal price_per_night;
}