package com.example.Hotel_Management_Frontend.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Hotel_Management_Frontend.dto.Reservation;
import com.example.Hotel_Management_Frontend.dto.ReservationDetailsDTO;
import com.example.Hotel_Management_Frontend.dto.CreateReservationDTO;
import com.example.Hotel_Management_Frontend.service.ReservationService;

@Controller
public class ReservationController {


    @Autowired
    private ReservationService reservationService;

@GetMapping("/reservations")
public String getReservations(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String guestName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String checkIn,
        @RequestParam(required = false) String checkOut,
        Model model) {

    List<Reservation> reservations = reservationService.searchReservations(page, guestName, email, checkIn, checkOut);

    model.addAttribute("reservations", reservations);
    model.addAttribute("currentPage", page);

    // 🔥 IMPORTANT (to keep filters while paging)
    model.addAttribute("guestName", guestName);
    model.addAttribute("email", email);
    model.addAttribute("checkIn", checkIn);
    model.addAttribute("checkOut", checkOut);

    return "reservation/reservations";
}

@GetMapping("/reservations/{id}/details")
public String getReservationDetails(@PathVariable int id, Model model) {

    ReservationDetailsDTO details = reservationService.getReservationDetails(id);

    model.addAttribute("res", details);

    return "reservation/reservationdetails";
}

    // 🔥 SHOW FORM
    @GetMapping("/reservations/add")
    public String showAddForm(Model model) {
        model.addAttribute("reservation", new Reservation());
        return "reservation/add-reservation";
    }

    // 🔥 SUBMIT FORM
    @PostMapping("/reservations/add")
    public String addReservation(@ModelAttribute CreateReservationDTO reservation) {
        System.out.print(reservation);
        reservationService.addReservation(reservation);

        return "redirect:/reservations";
    }

    @PostMapping("/reservations/delete/{id}")
    public String deleteReservation(@PathVariable int id) {
        reservationService.deleteReservation(id);
        return "redirect:/reservations"; // reload list
    }
}
    

