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
        String basePath = "/hotels?page=" + page + "&size=" + size;
        if (city != null && !city.isEmpty()) {
            basePath = "/hotels/search/findByLocation?location=" + city + "&page=" + page + "&size=" + size;
        } else if (name != null && !name.isEmpty()) {
            basePath = "/hotels/search/findByName?name=" + name + "&page=" + page + "&size=" + size;
        }

        return fetchHotelResponse(basePath);
    }

    public Hotel getHotelById(int id) {
        return fetchHotel(baseUrl + "/hotels/" + id, baseUrl + "/api/hotels/" + id);
    }

    public List<Amenity> getAmenitiesForHotel(int id) {
        try {
            AmenityResponse response = fetchAmenityResponse(
                    baseUrl + "/hotels/" + id + "/amenities",
                    baseUrl + "/api/hotels/" + id + "/amenities");
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

            ResponseEntity<Hotel> createResponse = postHotel(payload);

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
            exchangeWithFallback(baseUrl + "/hotels/" + hotelId, baseUrl + "/api/hotels/" + hotelId, entity);

            updateHotelAmenities(hotelId, amenityNames);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteHotel(int hotelId) {
        try {
            deleteWithFallback(baseUrl + "/hotels/" + hotelId, baseUrl + "/api/hotels/" + hotelId);
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
        putWithFallback(baseUrl + "/hotels/" + hotelId + "/amenities",
                baseUrl + "/api/hotels/" + hotelId + "/amenities", entity);
    }

    private String findOrCreateAmenityLink(String amenityName) {
        try {
            String searchPath = "/amenities/search/findByName?name=" +
                    java.net.URLEncoder.encode(amenityName, java.nio.charset.StandardCharsets.UTF_8);
            AmenityResponse response = fetchAmenityResponse(
                    baseUrl + searchPath,
                    baseUrl + "/api" + searchPath);
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
            ResponseEntity<Amenity> createResponse = postAmenity(payload);
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
        HotelResponse response = fetchHotelResponse("/hotels?page=0&size=1000");

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
        String basePath = "/hotels?page=" + page + "&size=" + size;

        if (amenity != null && !amenity.isEmpty()) {
            basePath = "/hotels/search/findByAmenityName?amenity=" + amenity + "&page=" + page + "&size=" + size;
        } else if (city != null && !city.isEmpty()) {
            basePath = "/hotels/search/findByLocation?location=" + city + "&page=" + page + "&size=" + size;
        } else if (name != null && !name.isEmpty()) {
            basePath = "/hotels/search/findByName?name=" + name + "&page=" + page + "&size=" + size;
        }

        return fetchHotelResponse(basePath);
    }

    public List<String> getAllAmenityNames() {
        try {
            String raw = getForObjectWithFallback(baseUrl + "/amenities?size=100", baseUrl + "/api/amenities?size=100");
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

    private HotelResponse fetchHotelResponse(String path) {
        return getForObjectWithFallback(
                baseUrl + path,
                baseUrl + "/api" + path,
                HotelResponse.class);
    }

    private Hotel fetchHotel(String url, String fallbackUrl) {
        return getForObjectWithFallback(url, fallbackUrl, Hotel.class);
    }

    private AmenityResponse fetchAmenityResponse(String url, String fallbackUrl) {
        return getForObjectWithFallback(url, fallbackUrl, AmenityResponse.class);
    }

    private ResponseEntity<Hotel> postHotel(java.util.Map<String, Object> payload) {
        return postForEntityWithFallback(
                baseUrl + "/hotels",
                baseUrl + "/api/hotels",
                payload,
                Hotel.class);
    }

    private ResponseEntity<Amenity> postAmenity(java.util.Map<String, Object> payload) {
        return postForEntityWithFallback(
                baseUrl + "/amenities",
                baseUrl + "/api/amenities",
                payload,
                Amenity.class);
    }

    private <T> T getForObjectWithFallback(String url, String fallbackUrl, Class<T> type) {
        try {
            return restTemplate.getForObject(url, type);
        } catch (Exception ex) {
            try {
                return restTemplate.getForObject(fallbackUrl, type);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String getForObjectWithFallback(String url, String fallbackUrl) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception ex) {
            try {
                return restTemplate.getForObject(fallbackUrl, String.class);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private <T> ResponseEntity<T> postForEntityWithFallback(String url, String fallbackUrl, Object payload, Class<T> type) {
        try {
            return restTemplate.postForEntity(url, payload, type);
        } catch (Exception ex) {
            return restTemplate.postForEntity(fallbackUrl, payload, type);
        }
    }

    private void exchangeWithFallback(String url, String fallbackUrl, HttpEntity<?> entity) {
        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (Exception ex) {
            restTemplate.exchange(fallbackUrl, HttpMethod.PUT, entity, Void.class);
        }
    }

    private void putWithFallback(String url, String fallbackUrl, HttpEntity<String> entity) {
        try {
            restTemplate.put(url, entity);
        } catch (Exception ex) {
            restTemplate.put(fallbackUrl, entity);
        }
    }

    private void deleteWithFallback(String url, String fallbackUrl) {
        try {
            restTemplate.delete(url);
        } catch (Exception ex) {
            restTemplate.delete(fallbackUrl);
        }
    }
}
