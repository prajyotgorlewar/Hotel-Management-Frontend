package com.example.Hotel_Management_Frontend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateReservationDTO {

    private String guestName;
    private String guestEmail;

    @JsonProperty("guest_phone")
    private String guestPhone;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private RoomDTO room; 
    
}
