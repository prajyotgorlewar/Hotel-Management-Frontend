package com.example.Hotel_Management_Frontend.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class HotelController {
    @GetMapping("/hotels")
    public String hotels() {
        return "hotel/hotels";
    }
}
