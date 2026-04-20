package com.example.Hotel_Management_Frontend.service;

import com.example.Hotel_Management_Frontend.dto.HotelDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HotelService1 {

    @Autowired
    private RestTemplate restTemplate;

    private final String baseUrl;
    public HotelService1(@Value("${backend.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // ── HAL response wrappers ─────────────────────────────────────────────────

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    static class HalLink { private String href; }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    static class HotelHal {
        private Integer hotelId;
        private String name;
        private String location;
        private String description;

        @JsonProperty("_links")
        private Map<String, HalLink> links;

        public Integer extractId() {
            if (hotelId != null) return hotelId;
            if (links != null && links.containsKey("self")) {
                String href = links.get("self").getHref();
                String[] parts = href.replaceAll("\\{.*\\}", "").trim().split("/");
                try { return Integer.parseInt(parts[parts.length - 1].trim()); }
                catch (NumberFormatException ignored) {}
            }
            return null;
        }

        public HotelDTO toDTO() {
            HotelDTO dto = new HotelDTO();
            dto.setHotelId(extractId());
            dto.setName(name);
            dto.setLocation(location);
            dto.setDescription(description);
            return dto;
        }
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    static class HotelEmbedded { private List<HotelHal> hotels; }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    static class HotelPage {
        @JsonProperty("_embedded")
        private HotelEmbedded embedded;
    }

    // ── READ: all hotels ─────────────────────────────────────────────────────
    public List<HotelDTO> getAllHotels() {
        String url = baseUrl + "/hotels?size=100";
        HotelPage page = restTemplate.getForObject(url, HotelPage.class);
        if (page == null || page.getEmbedded() == null
                || page.getEmbedded().getHotels() == null) return List.of();
        List<HotelDTO> result = new ArrayList<>();
        for (HotelHal h : page.getEmbedded().getHotels()) result.add(h.toDTO());
        return result;
    }

    // ── READ: single hotel ───────────────────────────────────────────────────
    public HotelDTO getHotelById(Integer hotelId) {
        String url =baseUrl + "/hotels/" + hotelId;
        HotelHal hal = restTemplate.getForObject(url, HotelHal.class);
        if (hal == null) return null;
        HotelDTO dto = hal.toDTO();
        dto.setHotelId(hotelId);
        return dto;
    }
}