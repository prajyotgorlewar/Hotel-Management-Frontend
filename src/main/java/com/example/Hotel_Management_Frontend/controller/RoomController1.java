package com.example.Hotel_Management_Frontend.controller;

import com.example.Hotel_Management_Frontend.dto.RoomDTO;
import com.example.Hotel_Management_Frontend.service.RoomService;
import com.example.Hotel_Management_Frontend.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hotels/{hotelId}/rooms")
public class RoomController1 {

    @Autowired
    private RoomService roomService1;

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping
    public String listRooms(@PathVariable Integer hotelId, Model model) {
        model.addAttribute("rooms", roomService1.getRoomsByHotel(hotelId));
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("newRoom", new RoomDTO());
        return "room/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Integer hotelId, Model model) {
        model.addAttribute("hotelId", hotelId);
        RoomDTO room = new RoomDTO();
        room.setHotelId(hotelId);
        model.addAttribute("room", room);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        return "room/form";
    }

    @PostMapping
    public String createRoom(
            @PathVariable Integer hotelId,
            @ModelAttribute RoomDTO roomDTO,
            RedirectAttributes ra) {
        try {
            roomService1.createRoom(hotelId, roomDTO);
            ra.addFlashAttribute("successMsg", "Room created successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return "redirect:/hotels/" + hotelId + "/rooms";
    }

    @GetMapping("/{roomId}/edit")
    public String showEditForm(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            Model model) {
        model.addAttribute("hotelId", hotelId);
        RoomDTO room = roomService1.getRoomById(roomId);
        if (room != null) {
            room.setHotelId(hotelId);
        }
        model.addAttribute("room", room);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        return "room/form";
    }

    @PostMapping("/{roomId}/update")
    public String updateRoom(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            @ModelAttribute RoomDTO roomDTO,
            RedirectAttributes ra) {
        try {
            roomService1.updateRoom(hotelId, roomId, roomDTO);
            ra.addFlashAttribute("successMsg", "Room updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return "redirect:/hotels/" + hotelId + "/rooms";
    }

    @PostMapping("/{roomId}/delete")
    public String deleteRoom(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            RedirectAttributes ra) {
        try {
            roomService1.deleteRoom(roomId);
            ra.addFlashAttribute("successMsg", "Room deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return "redirect:/hotels/" + hotelId + "/rooms";
    }
}

