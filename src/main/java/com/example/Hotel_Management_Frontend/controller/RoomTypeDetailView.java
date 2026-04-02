package com.example.Hotel_Management_Frontend.controller;

import java.util.Collections;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Hotel_Management_Frontend.dto.RoomType;
import com.example.Hotel_Management_Frontend.service.RoomService;
import com.example.Hotel_Management_Frontend.service.RoomTypeService;

@Controller
public class RoomTypeDetailView {

    private RoomTypeService roomTypeService;
    private RoomService roomService;

    public RoomTypeDetailView(RoomTypeService roomTypeService, RoomService roomService) {
        this.roomTypeService = roomTypeService;
        this.roomService = roomService;
    }

    @GetMapping("/room-type-detail")
    public String roomTypeDetailPage(@RequestParam("id") Integer id, Model model) {
        RoomType roomType = roomTypeService.getRoomTypeById(id);
        if (roomType == null) {
            return "redirect:/roomtypes?toast=error";
        }
        model.addAttribute("roomType", roomType);
        model.addAttribute("rooms", roomService.getRoomsByRoomType(id));
        return "roomtype/roomTypeDetail";
    }
}
