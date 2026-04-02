package com.example.Hotel_Management_Frontend.dto.PaymentDTO;

import lombok.Data;

@Data
public class UpdatePaymentDTO {
    private Double amount;
    private String paymentDate;
    private String paymentStatus;
    private String reservation;
}