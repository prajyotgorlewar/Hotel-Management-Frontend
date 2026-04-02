package com.example.Hotel_Management_Frontend.dto.PaymentDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWrapper {

    @JsonProperty("_embedded")
    private Embedded embedded;

    public List<PaymentDTO> getPayments() {
        return embedded != null ? embedded.getPayments() : List.of();
    }

    @Data                              // ← this was missing
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Embedded {
        private List<PaymentDTO> payments;
    }
}