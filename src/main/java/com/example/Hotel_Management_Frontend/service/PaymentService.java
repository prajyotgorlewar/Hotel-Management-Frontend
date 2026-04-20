package com.example.Hotel_Management_Frontend.service;

import com.example.Hotel_Management_Frontend.dto.PaymentDTO.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PaymentService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentService(RestTemplate restTemplate, @Value("${backend.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<PaymentDTO> getPaymentsByHotel(Long hotelId) {
        String url = baseUrl + "/payments/search/by-hotel?hotelId=" + hotelId;
        PaymentWrapper wrapper = restTemplate.getForObject(url, PaymentWrapper.class);
        return wrapper != null ? wrapper.getPayments() : List.of();
    }

    public void createPayment(CreatePaymentDTO dto) {
        String url = baseUrl +"/payments";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreatePaymentDTO> request = new HttpEntity<>(dto, headers);
        restTemplate.postForObject(url, request, String.class);
    }

    public PaymentDetailsDTO getPaymentById(Long paymentId) {
        String url = baseUrl +"/payments/search/by-payment-id?paymentId=" + paymentId;
        return restTemplate.getForObject(url, PaymentDetailsDTO.class);
    }

    public void updatePayment(Long paymentId, UpdatePaymentDTO dto) {
        String url = baseUrl + "/payments/" + paymentId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdatePaymentDTO> request = new HttpEntity<>(dto, headers);
        restTemplate.patchForObject(url, request, String.class);
    }
}
