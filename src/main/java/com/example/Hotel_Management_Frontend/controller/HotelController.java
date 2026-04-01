package com.example.Hotel_Management_Frontend.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Hotel_Management_Frontend.dto.Hotel;
import com.example.Hotel_Management_Frontend.dto.HotelResponse;
import com.example.Hotel_Management_Frontend.service.HotelService;

@Controller
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/hotels")
    public String getHotels(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "city", defaultValue = "") String city,
            @RequestParam(name = "amenity", defaultValue = "") String amenity,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "4") int size,
            Model model) {

        String resolvedName = name;
        String resolvedAmenity = amenity;

        if (!name.isEmpty() && amenity.isEmpty()) {
            String matched = hotelService.getAllAmenityNames().stream()
                    .filter(a -> a.equalsIgnoreCase(name.trim()))
                    .findFirst().orElse("");
            if (!matched.isEmpty()) {
                resolvedAmenity = matched;
                resolvedName = "";
            }
        }

        HotelResponse response = hotelService.getHotels(page, size, resolvedName, city, resolvedAmenity);

        model.addAttribute("hotels", response != null && response.getEmbedded() != null
                ? response.getEmbedded().getHotels()
                : Collections.emptyList());
        model.addAttribute("totalPages", response != null && response.getPage() != null
                ? response.getPage().getTotalPages()
                : 1);
        model.addAttribute("totalElements", response != null && response.getPage() != null
                ? response.getPage().getTotalElements()
                : 0);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("name", resolvedName);
        model.addAttribute("city", city);
        model.addAttribute("amenity", resolvedAmenity);
        model.addAttribute("cities", hotelService.getAllCities());

        return "hotel/hotelList";
    }

    @GetMapping("/hotels/{id}/details")
    public String getHotelDetails(@PathVariable int id, Model model) {

        Hotel hotel = hotelService.getHotelById(id);
        model.addAttribute("amenities", hotelService.getAmenitiesForHotel(id));
        model.addAttribute("hotel", hotel);

        return "hotel/hotelDetail";
    }
}
