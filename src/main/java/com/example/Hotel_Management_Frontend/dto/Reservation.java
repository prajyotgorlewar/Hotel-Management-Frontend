package com.example.Hotel_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reservation {

    @JsonProperty("reservation_id")
    private int id;

    private String guestName;
    private String guestEmail;

    @JsonProperty("guest_phone")
    private String phone;

    private String checkInDate;
    private String checkOutDate;

}

