package com.example.Hotel_Management_Frontend.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.Hotel_Management_Frontend.dto.Hotel;
import com.example.Hotel_Management_Frontend.dto.HotelResponse;
import com.example.Hotel_Management_Frontend.dto.Amenity;
import com.example.Hotel_Management_Frontend.dto.AmenityResponse;

@Service
public class HotelService {

    private final String BASE_URL = "http://172.16.160.110:8081";
    private final RestTemplate restTemplate = new RestTemplate();

    public HotelResponse getHotels(int page, int size, String name, String city) {
        String url = BASE_URL + "/hotels?page=" + page + "&size=" + size;

        if (city != null && !city.isEmpty()) {
            url = BASE_URL + "/hotels/search/findByLocation?location=" + city + "&page=" + page + "&size=" + size;
        } else if (name != null && !name.isEmpty()) {
            url = BASE_URL + "/hotels/search/findByName?name=" + name + "&page=" + page + "&size=" + size;
        }

        return restTemplate.getForObject(url, HotelResponse.class);
    }

    public Hotel getHotelById(int id) {
        return restTemplate.getForObject(BASE_URL + "/hotels/" + id, Hotel.class);
    }

    public List<Amenity> getAmenitiesForHotel(int id) {
        try {
            AmenityResponse response = restTemplate.getForObject(
                    BASE_URL + "/hotels/" + id + "/amenities",
                    AmenityResponse.class);
            if (response != null && response.getEmbedded() != null) {
                return response.getEmbedded().getAmenities();
            }
        } catch (Exception e) {
            // fall through to empty list
        }
        return Collections.emptyList();
    }

    public List<String> getAllCities() {
        String url = BASE_URL + "/hotels?page=0&size=1000";
        HotelResponse response = restTemplate.getForObject(url, HotelResponse.class);

        if (response != null && response.getEmbedded() != null) {
            return response.getEmbedded().getHotels().stream()
                    .map(Hotel::getLocation)
                    .filter(loc -> loc != null && !loc.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }

        return Collections.emptyList();
    }

    public HotelResponse getHotels(int page, int size, String name, String city, String amenity) {
        String url = BASE_URL + "/hotels?page=" + page + "&size=" + size;

        if (amenity != null && !amenity.isEmpty()) {
            url = BASE_URL + "/hotels/search/findByAmenityName?amenity=" + amenity + "&page=" + page + "&size=" + size;
        } else if (city != null && !city.isEmpty()) {
            url = BASE_URL + "/hotels/search/findByLocation?location=" + city + "&page=" + page + "&size=" + size;
        } else if (name != null && !name.isEmpty()) {
            url = BASE_URL + "/hotels/search/findByName?name=" + name + "&page=" + page + "&size=" + size;
        }

        return restTemplate.getForObject(url, HotelResponse.class);
    }

    public List<String> getAllAmenityNames() {
        try {
            String raw = restTemplate.getForObject(BASE_URL + "/amenities?size=100", String.class);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(raw);
            List<String> names = new java.util.ArrayList<>();
            root.path("_embedded").path("amenities")
                    .forEach(a -> names.add(a.path("name").asText()));
            return names;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
