package com.example.Hotel_Management_Frontend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RoomService {

    private final String BASE_URL = "http://172.16.160.110:8081";
    private final RestTemplate rt = new RestTemplate();

    public String getAllRooms(int size) {
        return rt.getForObject(BASE_URL + "/rooms?size=" + size, String.class);
    }

    public String getRoomsByRoomType(String roomTypeId) {
        return rt.getForObject(
            BASE_URL + "/rooms/search/findByRoomType_RoomTypeId?roomTypeId=" + roomTypeId + "&size=1000", 
            String.class
        );
    }
}