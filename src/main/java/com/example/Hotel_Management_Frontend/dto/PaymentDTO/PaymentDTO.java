package com.example.Hotel_Management_Frontend.dto.PaymentDTO;

import lombok.Data;

@Data
public class PaymentDTO {
    private Long paymentId;
    private Double amount;
//    private String checkInDate;
//    private String checkOutDate;
//    private String guestEmail;
//    private String guestName;
//    private String guestPhone;
//    private String hotelLocation;
//    private String hotelName;
    private String paymentDate;
    private String paymentStatus;
//    private Integer roomNumber;

    // Getters and Setters
}