package com.example.Hotel_Management_Frontend.service;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.example.Hotel_Management_Frontend.dto.Amenity;
import com.example.Hotel_Management_Frontend.dto.AmenityResponse;
import com.example.Hotel_Management_Frontend.dto.Hotel;
import com.example.Hotel_Management_Frontend.dto.HotelResponse;

@Service
public class HotelService {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public HotelService(@Value("${backend.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HotelResponse getHotels(int page, int size, String name, String city) {
        String url = baseUrl + "/hotels?page=" + page + "&size=" + size;

        if (city != null && !city.isEmpty()) {
            url = baseUrl + "/hotels/search/findByLocation?location=" + city + "&page=" + page + "&size=" + size;
        } else if (name != null && !name.isEmpty()) {
            url = baseUrl + "/hotels/search/findByName?name=" + name + "&page=" + page + "&size=" + size;
        }

        return restTemplate.getForObject(url, HotelResponse.class);
    }

    public Hotel getHotelById(int id) {
        return restTemplate.getForObject(baseUrl + "/hotels/" + id, Hotel.class);
    }

    public List<Amenity> getAmenitiesForHotel(int id) {
        try {
            AmenityResponse response = restTemplate.getForObject(
                    baseUrl + "/hotels/" + id + "/amenities",
                    AmenityResponse.class);
            if (response != null && response.getEmbedded() != null) {
                return response.getEmbedded().getAmenities();
            }
        } catch (Exception e) {
            // fall through to empty list
        }
        return Collections.emptyList();
    }

    public Hotel createHotel(String name, String location, String description, List<String> amenityNames) {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("name", name);
            payload.put("location", location);
            payload.put("description", description);

            ResponseEntity<Hotel> createResponse = restTemplate.postForEntity(
                    baseUrl + "/hotels",
                    payload,
                    Hotel.class);

            Hotel created = createResponse.getBody();
            Integer hotelId = created != null ? created.getResolvedId() : null;
            if (hotelId == null && createResponse.getHeaders().getLocation() != null) {
                String locationHeader = createResponse.getHeaders().getLocation().toString();
                int lastSlash = locationHeader.lastIndexOf('/');
                if (lastSlash > -1 && lastSlash < locationHeader.length() - 1) {
                    try {
                        hotelId = Integer.valueOf(locationHeader.substring(lastSlash + 1));
                    } catch (NumberFormatException ex) {
                        hotelId = null;
                    }
                }
            }

            if (hotelId != null) {
                updateHotelAmenities(hotelId, amenityNames);
            }

            return hotelId != null ? getHotelById(hotelId) : created;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean updateHotel(int hotelId, String name, String location, String description, List<String> amenityNames) {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("name", name);
            payload.put("location", location);
            payload.put("description", description);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.exchange(baseUrl + "/hotels/" + hotelId, HttpMethod.PUT, entity, Void.class);

            updateHotelAmenities(hotelId, amenityNames);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteHotel(int hotelId) {
        try {
            restTemplate.delete(baseUrl + "/hotels/" + hotelId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateHotelAmenities(int hotelId, List<String> amenityNames) {
        if (amenityNames == null) {
            return;
        }
        List<String> amenityLinks = new java.util.ArrayList<>();
        for (String raw : amenityNames) {
            String amenityName = raw != null ? raw.trim() : "";
            if (amenityName.isEmpty()) {
                continue;
            }
            String link = findOrCreateAmenityLink(amenityName);
            if (link != null) {
                amenityLinks.add(link);
            }
        }

        String body = String.join("\n", amenityLinks);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/uri-list"));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        restTemplate.put(baseUrl + "/hotels/" + hotelId + "/amenities", entity);
    }

    private String findOrCreateAmenityLink(String amenityName) {
        try {
            String searchUrl = baseUrl + "/amenities/search/findByName?name=" +
                    java.net.URLEncoder.encode(amenityName, java.nio.charset.StandardCharsets.UTF_8);
            AmenityResponse response = restTemplate.getForObject(searchUrl, AmenityResponse.class);
            if (response != null && response.getEmbedded() != null) {
                List<Amenity> amenities = response.getEmbedded().getAmenities();
                if (amenities != null && !amenities.isEmpty()) {
                    return amenities.get(0).getSelfHref();
                }
            }
        } catch (Exception e) {
            // continue to create
        }

        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("name", amenityName);
            ResponseEntity<Amenity> createResponse = restTemplate.postForEntity(
                    baseUrl + "/amenities",
                    payload,
                    Amenity.class);
            Amenity created = createResponse.getBody();
            if (created != null && created.getSelfHref() != null) {
                return created.getSelfHref();
            }
            if (createResponse.getHeaders().getLocation() != null) {
                return createResponse.getHeaders().getLocation().toString();
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public List<String> getAllCities() {
        String url = baseUrl + "/hotels?page=0&size=1000";
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
        String url = baseUrl + "/hotels?page=" + page + "&size=" + size;

        if (amenity != null && !amenity.isEmpty()) {
            url = baseUrl + "/hotels/search/findByAmenityName?amenity=" + amenity + "&page=" + page + "&size=" + size;
        } else if (city != null && !city.isEmpty()) {
            url = baseUrl + "/hotels/search/findByLocation?location=" + city + "&page=" + page + "&size=" + size;
        } else if (name != null && !name.isEmpty()) {
            url = baseUrl + "/hotels/search/findByName?name=" + name + "&page=" + page + "&size=" + size;
        }

        return restTemplate.getForObject(url, HotelResponse.class);
    }

    public List<String> getAllAmenityNames() {
        try {
            String raw = restTemplate.getForObject(baseUrl + "/amenities?size=100", String.class);
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
