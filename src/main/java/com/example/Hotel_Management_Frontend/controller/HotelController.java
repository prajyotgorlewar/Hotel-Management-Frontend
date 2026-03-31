package com.example.Hotel_Management_Frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HotelController {
    @GetMapping("/hotels")
    public String hotels() {
        return "hotel/hotelList";
    }

    @GetMapping("/hotels/details")
    public String hotelDetails() {
        return "hotel/hoteldetails";
    }
}
