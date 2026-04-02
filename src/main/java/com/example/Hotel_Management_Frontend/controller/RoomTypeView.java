package com.example.Hotel_Management_Frontend.controller;

import java.math.BigDecimal;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Hotel_Management_Frontend.dto.RoomTypeResponse;
import com.example.Hotel_Management_Frontend.service.RoomTypeService;

@Controller
public class RoomTypeView {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping({"/roomtypes", "/room-types"})
    public String roomTypesPage(
            @RequestParam(name = "toast", defaultValue = "") String toast,
            Model model) {
        RoomTypeResponse response = roomTypeService.getRoomTypes(1000);
        model.addAttribute("roomTypes", response != null && response.getEmbedded() != null
                ? response.getEmbedded().getRoomTypes()
                : Collections.emptyList());
        model.addAttribute("toast", toast);
        return "roomtype/roomTypeList";
    }

    @PostMapping("/roomtypes/create")
    public String createRoomType(
            @RequestParam("typeName") String typeName,
            @RequestParam(name = "description", defaultValue = "") String description,
            @RequestParam("maxOccupancy") Integer maxOccupancy,
            @RequestParam("pricePerNight") BigDecimal pricePerNight) {
        boolean ok = roomTypeService.createRoomType(typeName, description, maxOccupancy, pricePerNight) != null;
        return "redirect:/roomtypes?toast=" + (ok ? "created" : "error");
    }

    @PostMapping("/roomtypes/update")
    public String updateRoomType(
            @RequestParam("id") Integer id,
            @RequestParam("typeName") String typeName,
            @RequestParam(name = "description", defaultValue = "") String description,
            @RequestParam("maxOccupancy") Integer maxOccupancy,
            @RequestParam("pricePerNight") BigDecimal pricePerNight) {
        boolean ok = roomTypeService.updateRoomType(id, typeName, description, maxOccupancy, pricePerNight);
        return "redirect:/roomtypes?toast=" + (ok ? "updated" : "error");
    }

    @PostMapping("/roomtypes/delete")
    public String deleteRoomType(@RequestParam("id") Integer id) {
        boolean ok = roomTypeService.deleteRoomTypeById(id);
        return "redirect:/roomtypes?toast=" + (ok ? "deleted" : "error");
    }
}
