package com.example.Hotel_Management_Frontend.controller;

//import CreatePaymentDTO;
import com.example.Hotel_Management_Frontend.dto.PaymentDTO.CreatePaymentDTO;
import com.example.Hotel_Management_Frontend.dto.PaymentDTO.PaymentDTO;
//import PaymentDetailsDTO;
import com.example.Hotel_Management_Frontend.dto.PaymentDTO.PaymentDetailsDTO;
import com.example.Hotel_Management_Frontend.dto.PaymentDTO.UpdatePaymentDTO;
import com.example.Hotel_Management_Frontend.dto.HotelResponse;
import com.example.Hotel_Management_Frontend.service.HotelService;
import com.example.Hotel_Management_Frontend.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hotel-payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final HotelService hotelService;
    @Value("${backend.base-url}")
    private String backendBaseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentController(PaymentService paymentService, HotelService hotelService) {
        this.paymentService = paymentService;
        this.hotelService = hotelService;
    }

    @GetMapping
    public String showPayments(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        try {
            List<PaymentDTO> allPayments = paymentService.getPaymentsByHotel(hotelId);

            // Manual pagination since you're fetching from a service (not direct DB)
            int totalElements = allPayments.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);

            List<PaymentDTO> pagedPayments = (start >= totalElements)
                    ? List.of()
                    : allPayments.subList(start, end);

            model.addAttribute("payments", pagedPayments);
            model.addAttribute("allPayments", allPayments); // for total revenue
            model.addAttribute("hotelId", hotelId);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("size", size);
            return "payment/payments";

        } catch (HttpClientErrorException.NotFound e) {
            model.addAttribute("errorMessage", "No payments found for hotel ID: " + hotelId);
            model.addAttribute("payments", List.of());
            model.addAttribute("allPayments", List.of());
            model.addAttribute("hotelId", hotelId);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            return "payment/payments";
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Client error: " + e.getStatusCode());
            return "payment/error";
        } catch (HttpServerErrorException e) {
            model.addAttribute("errorMessage", "Server error. Please try again later.");
            return "payment/error";
        } catch (ResourceAccessException e) {
            model.addAttribute("errorMessage", "Unable to connect to the payment service.");
            return "payment/error";
        }
    }
    @GetMapping("/create")
    public String showCreateForm(@RequestParam Long hotelId, Model model) {
        try {
            model.addAttribute("hotelId", hotelId);
            model.addAttribute("createPaymentDTO", new CreatePaymentDTO());
            return "payment/create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to load the payment creation form.");
            return "payment/error";
        }
    }

    @PostMapping("/create")
    public String submitCreateForm(
            @RequestParam Long hotelId,
            @RequestParam Long reservationId,
            @ModelAttribute CreatePaymentDTO createPaymentDTO,
            RedirectAttributes redirectAttributes) {
        try {
            if (!reservationExists(reservationId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reservation not found for ID: " + reservationId);
                return "redirect:/hotel-payments/create?hotelId=" + hotelId;
            }
            createPaymentDTO.setReservation(backendBaseUrl + "/reservations/" + reservationId);
            paymentService.createPayment(createPaymentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Payment created successfully.");
            return "redirect:/hotel-payments?hotelId=" + hotelId;
        } catch (HttpClientErrorException.BadRequest e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid payment data. Please check your inputs.");
            return "redirect:/hotel-payments/create?hotelId=" + hotelId;
        } catch (HttpClientErrorException.NotFound e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reservation not found for ID: " + reservationId);
            return "redirect:/hotel-payments/create?hotelId=" + hotelId;
        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Client error while creating payment: " + e.getStatusCode());
            return "redirect:/hotel-payments/create?hotelId=" + hotelId;
        } catch (HttpServerErrorException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Server error while creating payment. Please try again later.");
            return "redirect:/hotel-payments/create?hotelId=" + hotelId;
        } catch (ResourceAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to connect to payment service. Please try again.");
            return "redirect:/hotel-payments/create?hotelId=" + hotelId;
        }
    }

    @GetMapping("/view")
    public String viewPayment(@RequestParam Long paymentId, Model model) {
        try {
            PaymentDetailsDTO payment = paymentService.getPaymentById(paymentId);
            model.addAttribute("payment", payment);
            return "payment/view";
        } catch (HttpClientErrorException.NotFound e) {
            model.addAttribute("errorMessage", "Payment not found for ID: " + paymentId);
            return "payment/error";
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Client error while fetching payment: " + e.getStatusCode());
            return "payment/error";
        } catch (HttpServerErrorException e) {
            model.addAttribute("errorMessage", "Server error while fetching payment. Please try again later.");
            return "payment/error";
        } catch (ResourceAccessException e) {
            model.addAttribute("errorMessage", "Unable to connect to the payment service.");
            return "payment/error";
        }
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam Long paymentId, Model model) {
        try {
            PaymentDetailsDTO payment = paymentService.getPaymentById(paymentId);
            UpdatePaymentDTO updatePaymentDTO = new UpdatePaymentDTO();
            updatePaymentDTO.setAmount(payment.getAmount());
            updatePaymentDTO.setPaymentDate(payment.getPaymentDate());
            updatePaymentDTO.setPaymentStatus(payment.getPaymentStatus());
            model.addAttribute("payment", payment);
            model.addAttribute("updatePaymentDTO", updatePaymentDTO);
            return "payment/edit";
        } catch (HttpClientErrorException.NotFound e) {
            model.addAttribute("errorMessage", "Payment not found for ID: " + paymentId);
            return "payment/error";
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Client error while loading edit form: " + e.getStatusCode());
            return "payment/error";
        } catch (HttpServerErrorException e) {
            model.addAttribute("errorMessage", "Server error while loading edit form. Please try again later.");
            return "payment/error";
        } catch (ResourceAccessException e) {
            model.addAttribute("errorMessage", "Unable to connect to the payment service.");
            return "payment/error";
        }
    }

    @PostMapping("/edit")
    public String submitEditForm(
            @RequestParam Long paymentId,
            @RequestParam Long reservationId,
            @ModelAttribute UpdatePaymentDTO updatePaymentDTO,
            RedirectAttributes redirectAttributes) {
        try {
            if (!reservationExists(reservationId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reservation not found for ID: " + reservationId);
                return "redirect:/hotel-payments/edit?paymentId=" + paymentId;
            }
            updatePaymentDTO.setReservation(backendBaseUrl + "/reservations/" + reservationId);
            paymentService.updatePayment(paymentId, updatePaymentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Payment updated successfully.");
            return "redirect:/hotel-payments/view?paymentId=" + paymentId;
        } catch (HttpClientErrorException.NotFound e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment not found for ID: " + paymentId);
            return "redirect:/hotel-payments/view?paymentId=" + paymentId;
        } catch (HttpClientErrorException.BadRequest e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid update data. Please check your inputs.");
            return "redirect:/hotel-payments/edit?paymentId=" + paymentId;
        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Client error while updating payment: " + e.getStatusCode());
            return "redirect:/hotel-payments/edit?paymentId=" + paymentId;
        } catch (HttpServerErrorException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Server error while updating payment. Please try again later.");
            return "redirect:/hotel-payments/edit?paymentId=" + paymentId;
        } catch (ResourceAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to connect to payment service. Please try again.");
            return "redirect:/hotel-payments/edit?paymentId=" + paymentId;
        }
    }

    @GetMapping("/list")
    public String demo(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "city", defaultValue = "") String city,
            @RequestParam(name = "amenity", defaultValue = "") String amenity,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "4") int size,
            Model model){

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
                : java.util.Collections.emptyList());
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

        return "payment/hotelpaymentlist";
    }

    private boolean reservationExists(Long reservationId) {
        if (reservationId == null) {
            return false;
        }
        String[] urls = new String[] {
                backendBaseUrl + "/reservations/" + reservationId,
                backendBaseUrl + "/reservation/" + reservationId,
                backendBaseUrl + "/api/reservations/" + reservationId
        };
        for (String url : urls) {
            try {
                restTemplate.getForObject(url, String.class);
                return true;
            } catch (Exception ex) {
                // try next
            }
        }
        return false;
    }
}
