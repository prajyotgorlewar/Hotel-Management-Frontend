package com.example.Hotel_Management_Frontend.dto.PaymentDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreatePaymentDTO {
    private Double amount;

//    @JsonProperty("payment_date")
    private String paymentDate;

//    @JsonProperty("payment_status")
    private String paymentStatus;

    private String reservation;
}