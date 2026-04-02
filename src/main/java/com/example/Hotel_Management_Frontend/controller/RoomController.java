package com.example.Hotel_Management_Frontend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Hotel_Management_Frontend.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService service;

    @GetMapping
    public String getAll(@RequestParam(defaultValue = "100") int size) {
        return service.getAllRooms(size);
    }

    @GetMapping("/search/byRoomType")
    public String getByRoomType(@RequestParam String roomTypeId) {
        return service.getRoomsByRoomType(roomTypeId);
    }
}