package com.example.Hotel_Management_Frontend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import com.example.Hotel_Management_Frontend.dto.Room;
import com.example.Hotel_Management_Frontend.dto.RoomResponse;

@Service
public class RoomService {

    private final String baseUrl;
    private final RestTemplate rt = new RestTemplate();

    public RoomService(@Value("${backend.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAllRooms(int size) {
        return rt.getForObject(baseUrl + "/rooms?size=" + size, String.class);
    }

    public String getRoomsByRoomType(String roomTypeId) {
        return rt.getForObject(
            baseUrl + "/rooms/search/findByRoomType_RoomTypeId?roomTypeId=" + roomTypeId + "&size=1000", 
            String.class
        );
    }

    public java.util.List<Room> getRoomsByRoomType(int roomTypeId) {
        try {
            RoomResponse response = rt.getForObject(
                baseUrl + "/rooms/search/byRoomType?roomTypeId=" + roomTypeId,
                RoomResponse.class
            );
            if (response != null && response.getEmbedded() != null) {
                return response.getEmbedded().getRooms();
            }
            return java.util.Collections.emptyList();
        } catch (HttpClientErrorException ex) {
            // fall through to generic fallback
        } catch (Exception ex) {
            // fall through to generic fallback
        }

        return loadAndFilterRoomsFallback(roomTypeId);
    }

    private java.util.List<Room> loadAndFilterRoomsFallback(int roomTypeId) {
        try {
            String raw = rt.getForObject(baseUrl + "/rooms?size=1000", String.class);
            if (raw == null || raw.isBlank()) {
                return java.util.Collections.emptyList();
            }
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(raw);
            java.util.List<Room> rooms = new java.util.ArrayList<>();

            if (root.isArray()) {
                java.util.List<Room> list = mapper.convertValue(
                    root,
                    mapper.getTypeFactory().constructCollectionType(java.util.List.class, Room.class)
                );
                rooms.addAll(list);
            } else if (root.has("_embedded") && root.path("_embedded").has("rooms")) {
                java.util.List<Room> list = mapper.convertValue(
                    root.path("_embedded").path("rooms"),
                    mapper.getTypeFactory().constructCollectionType(java.util.List.class, Room.class)
                );
                rooms.addAll(list);
            } else if (root.has("content")) {
                java.util.List<Room> list = mapper.convertValue(
                    root.path("content"),
                    mapper.getTypeFactory().constructCollectionType(java.util.List.class, Room.class)
                );
                rooms.addAll(list);
            }

            return rooms.stream()
                .filter(r -> r != null)
                .filter(r -> {
                    if (r.getRoomTypeId() != null) {
                        return r.getRoomTypeId().equals(roomTypeId);
                    }
                    if (r.getLinks() != null && r.getLinks().getRoomType() != null) {
                        String href = r.getLinks().getRoomType().getHref();
                        if (href != null) {
                            int lastSlash = href.lastIndexOf('/');
                            if (lastSlash > -1 && lastSlash < href.length() - 1) {
                                return href.substring(lastSlash + 1).equals(String.valueOf(roomTypeId));
                            }
                        }
                    }
                    return false;
                })
                .toList();
        } catch (Exception ex) {
            return java.util.Collections.emptyList();
        }
    }
}
