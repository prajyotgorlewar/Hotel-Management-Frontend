package com.example.Hotel_Management_Frontend.controller;

import com.example.Hotel_Management_Frontend.dto.AmenityDTO;
import com.example.Hotel_Management_Frontend.dto.RoomDTO;
import com.example.Hotel_Management_Frontend.service.AmenityService;
import com.example.Hotel_Management_Frontend.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hotels/{hotelId}/rooms/{roomId}/amenities")
public class AmenityController {

    private final AmenityService amenityService;
    private final RoomService roomService;

    public AmenityController(AmenityService amenityService, RoomService roomService) {
        this.amenityService = amenityService;
        this.roomService = roomService;
    }

    @GetMapping
    public String listAmenities(@PathVariable Integer hotelId, @PathVariable Integer roomId, Model model) {
        RoomDTO room = roomService.getRoomById(roomId);
        if (room != null && room.getHotelId() == null) {
            room.setHotelId(hotelId);
        }

        model.addAttribute("hotelId", hotelId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("room", room);
        model.addAttribute("amenities", amenityService.getAmenitiesByRoom(roomId));
        model.addAttribute("allAmenities", amenityService.getAllAmenities());
        return "amenity/list";
    }

    @PostMapping("/create")
    public String createAmenity(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            @ModelAttribute AmenityDTO amenityDTO,
            RedirectAttributes ra) {
        try {
            amenityService.createAndAssign(roomId, amenityDTO);
            ra.addFlashAttribute("successMsg", "Amenity created and assigned successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return redirectToList(hotelId, roomId);
    }

    @PostMapping("/assign")
    public String assignAmenity(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            @RequestParam Integer amenityId,
            RedirectAttributes ra) {
        try {
            amenityService.assignToRoom(roomId, amenityId);
            ra.addFlashAttribute("successMsg", "Amenity assigned successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return redirectToList(hotelId, roomId);
    }

    @PostMapping("/{amenityId}/update")
    public String updateAmenity(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            @PathVariable Integer amenityId,
            @ModelAttribute AmenityDTO amenityDTO,
            RedirectAttributes ra) {
        try {
            amenityService.updateAmenity(amenityId, amenityDTO);
            ra.addFlashAttribute("successMsg", "Amenity updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return redirectToList(hotelId, roomId);
    }

    @PostMapping("/{amenityId}/unassign")
    public String unassignAmenity(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            @PathVariable Integer amenityId,
            RedirectAttributes ra) {
        try {
            amenityService.unassignFromRoom(roomId, amenityId);
            ra.addFlashAttribute("successMsg", "Amenity removed from room successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return redirectToList(hotelId, roomId);
    }

    @PostMapping("/{amenityId}/delete")
    public String deleteAmenity(
            @PathVariable Integer hotelId,
            @PathVariable Integer roomId,
            @PathVariable Integer amenityId,
            RedirectAttributes ra) {
        try {
            amenityService.deleteAmenity(amenityId);
            ra.addFlashAttribute("successMsg", "Amenity deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return redirectToList(hotelId, roomId);
    }

    private String redirectToList(Integer hotelId, Integer roomId) {
        return "redirect:/hotels/" + hotelId + "/rooms/" + roomId + "/amenities";
    }
}
