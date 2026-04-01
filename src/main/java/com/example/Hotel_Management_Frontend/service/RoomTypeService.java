package com.example.Hotel_Management_Frontend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RoomTypeService {

    private final String BASE_URL = "http://172.16.160.110:8081";
    private final RestTemplate rt = new RestTemplate();

    public String getAllRoomTypes(int size) {
        return rt.getForObject(BASE_URL + "/roomtypes?size=" + size, String.class);
    }

    public String getRoomTypeById(String id) {
        return rt.getForObject(BASE_URL + "/roomtypes/" + id, String.class);
    }

    public String createRoomType(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return rt.postForObject(BASE_URL + "/roomtypes", entity, String.class);
    }

    public String updateRoomType(String id, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        rt.exchange(BASE_URL + "/roomtypes/" + id, HttpMethod.PUT, entity, String.class);
        return getRoomTypeById(id);
    }

    public ResponseEntity<Void> deleteRoomType(String id) {
        try {
            ResponseEntity<Void> response = rt.exchange(
                BASE_URL + "/roomtypes/" + id,
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
}