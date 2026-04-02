package com.example.Hotel_Management_Frontend.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.Hotel_Management_Frontend.dto.RoomType;
import com.example.Hotel_Management_Frontend.dto.RoomTypeResponse;

@Service
public class RoomTypeService {

    private final String baseUrl;
    private final RestTemplate rt = new RestTemplate();

    public RoomTypeService(@Value("${backend.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAllRoomTypes(int size) {
        return rt.getForObject(baseUrl + "/roomtypes?size=" + size, String.class);
    }

    public String getRoomTypeById(String id) {
        return rt.getForObject(baseUrl + "/roomtypes/" + id, String.class);
    }

    public RoomTypeResponse getRoomTypes(int size) {
        return rt.getForObject(baseUrl + "/roomtypes?size=" + size, RoomTypeResponse.class);
    }

    public RoomType getRoomTypeById(int id) {
        return rt.getForObject(baseUrl + "/roomtypes/" + id, RoomType.class);
    }

    public String createRoomType(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return rt.postForObject(baseUrl + "/roomtypes", entity, String.class);
    }

    public RoomType createRoomType(String typeName, String description, Integer maxOccupancy, BigDecimal pricePerNight) {
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("typeName", typeName);
            payload.put("description", description);
            payload.put("maxOccupancy", maxOccupancy);
            payload.put("pricePerNight", pricePerNight);

            ResponseEntity<RoomType> response = rt.postForEntity(baseUrl + "/roomtypes", payload, RoomType.class);
            RoomType created = response.getBody();
            Integer id = created != null ? created.getResolvedId() : null;
            if (id == null && response.getHeaders().getLocation() != null) {
                String location = response.getHeaders().getLocation().toString();
                int lastSlash = location.lastIndexOf('/');
                if (lastSlash > -1 && lastSlash < location.length() - 1) {
                    try {
                        id = Integer.valueOf(location.substring(lastSlash + 1));
                    } catch (NumberFormatException ex) {
                        id = null;
                    }
                }
            }
            return id != null ? getRoomTypeById(id) : created;
        } catch (Exception e) {
            return null;
        }
    }

    public String updateRoomType(String id, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        rt.exchange(baseUrl + "/roomtypes/" + id, HttpMethod.PUT, entity, String.class);
        return getRoomTypeById(id);
    }

    public boolean updateRoomType(int id, String typeName, String description, Integer maxOccupancy, BigDecimal pricePerNight) {
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("typeName", typeName);
            payload.put("description", description);
            payload.put("maxOccupancy", maxOccupancy);
            payload.put("pricePerNight", pricePerNight);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            rt.exchange(baseUrl + "/roomtypes/" + id, HttpMethod.PUT, entity, Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ResponseEntity<Void> deleteRoomType(String id) {
        try {
            ResponseEntity<Void> response = rt.exchange(
                baseUrl + "/roomtypes/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
            );
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public boolean deleteRoomTypeById(int id) {
        try {
            rt.delete(baseUrl + "/roomtypes/" + id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
