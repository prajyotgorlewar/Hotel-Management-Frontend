package com.example.Hotel_Management_Frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomTypeView {
    @GetMapping("/room-types")
    public String roomTypesPage() {
        return "roomtype/roomtypes";
    }
}