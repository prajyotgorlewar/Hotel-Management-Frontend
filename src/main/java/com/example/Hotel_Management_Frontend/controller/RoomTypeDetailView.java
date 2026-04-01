package com.example.Hotel_Management_Frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomTypeDetailView {
    @GetMapping("/room-type-detail")
    public String roomTypeDetailPage() {
        return "roomtype/roomtypedetail";
    }
}