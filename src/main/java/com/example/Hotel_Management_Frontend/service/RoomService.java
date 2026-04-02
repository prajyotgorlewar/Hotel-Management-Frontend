package com.example.Hotel_Management_Frontend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.Hotel_Management_Frontend.dto.Room;
import com.example.Hotel_Management_Frontend.dto.RoomDTO;
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

    public List<Room> getRoomsByRoomType(int roomTypeId) {
        try {
            RoomResponse response = rt.getForObject(
                baseUrl + "/rooms/search/byRoomType?roomTypeId=" + roomTypeId,
                RoomResponse.class
            );
            if (response != null && response.getEmbedded() != null) {
                return response.getEmbedded().getRooms();
            }
            return Collections.emptyList();
        } catch (HttpClientErrorException ex) {
            // fall through to fallback
        } catch (Exception ex) {
            // fall through to fallback
        }

        return loadAndFilterRoomsFallback(roomTypeId);
    }

    public List<RoomDTO> getRoomsByHotel(Integer hotelId) {
        try {
            List<RoomDTO> rooms = loadRoomDtosFromUrl(baseUrl + "/hotels/" + hotelId + "/rooms");
            if (!rooms.isEmpty()) {
                rooms.forEach(room -> hydrateDerivedFields(room, hotelId));
                return rooms;
            }
        } catch (Exception ex) {
            // fall through to alternate lookup
        }

        try {
            List<RoomDTO> rooms = loadRoomDtosFromUrl(
                baseUrl + "/rooms/search/findByHotel_HotelId?hotelId=" + hotelId + "&size=1000"
            );
            if (!rooms.isEmpty()) {
                rooms.forEach(room -> hydrateDerivedFields(room, hotelId));
                return rooms;
            }
        } catch (Exception ex) {
            // fall through to generic fallback
        }

        try {
            List<RoomDTO> rooms = loadRoomDtosFromUrl(baseUrl + "/rooms?size=1000");
            rooms.forEach(room -> hydrateDerivedFields(room, room != null ? room.getHotelId() : null));
            return rooms.stream()
                .filter(room -> room != null && hotelId.equals(room.getHotelId()))
                .toList();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public RoomDTO getRoomById(Integer roomId) {
        try {
            RoomDTO room = rt.getForObject(baseUrl + "/rooms/" + roomId, RoomDTO.class);
            hydrateDerivedFields(room, room != null ? room.getHotelId() : null);
            return room;
        } catch (Exception ex) {
            return null;
        }
    }

    public RoomDTO createRoom(Integer hotelId, RoomDTO roomDTO) {
        Integer roomId = saveRoom(hotelId, null, roomDTO, true);
        return roomId != null ? getRoomById(roomId) : null;
    }

    public void updateRoom(Integer hotelId, Integer roomId, RoomDTO roomDTO) {
        saveRoom(hotelId, roomId, roomDTO, false);
    }

    public void deleteRoom(Integer roomId) {
        rt.delete(baseUrl + "/rooms/" + roomId);
    }

    private Integer saveRoom(Integer hotelId, Integer roomId, RoomDTO roomDTO, boolean create) {
        if (roomDTO == null) {
            throw new IllegalArgumentException("Room details are required.");
        }
        if (roomDTO.getRoomNumber() == null) {
            throw new IllegalArgumentException("Room number is required.");
        }

        Integer roomTypeId = roomDTO.getRoomTypeId();
        if (roomTypeId == null && roomDTO.getRoomType() != null) {
            roomTypeId = roomDTO.getRoomType().getRoomTypeId();
        }
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type is required.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("roomNumber", roomDTO.getRoomNumber());
        payload.put("isAvailable", roomDTO.getIsAvailable() != null ? roomDTO.getIsAvailable() : Boolean.TRUE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        Integer savedRoomId = roomId;
        if (create) {
            ResponseEntity<Room> response = rt.postForEntity(baseUrl + "/rooms", entity, Room.class);
            savedRoomId = resolveRoomId(response.getBody(), response.getHeaders().getLocation() != null
                ? response.getHeaders().getLocation().toString()
                : null);
            if (savedRoomId == null) {
                throw new IllegalStateException("Room was created, but its id could not be resolved.");
            }
        } else {
            rt.exchange(baseUrl + "/rooms/" + roomId, HttpMethod.PUT, entity, Void.class);
        }

        updateRoomRelations(savedRoomId, hotelId, roomTypeId);
        return savedRoomId;
    }

    private void updateRoomRelations(Integer roomId, Integer hotelId, Integer roomTypeId) {
        putUriRelation(baseUrl + "/rooms/" + roomId + "/hotel", baseUrl + "/hotels/" + hotelId);
        putUriRelation(baseUrl + "/rooms/" + roomId + "/roomType", baseUrl + "/roomtypes/" + roomTypeId);
    }

    private void putUriRelation(String url, String resourceUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/uri-list"));
        HttpEntity<String> entity = new HttpEntity<>(resourceUri, headers);
        rt.put(url, entity);
    }

    private void hydrateDerivedFields(RoomDTO room, Integer fallbackHotelId) {
        if (room == null) {
            return;
        }
        if (room.getHotelId() == null) {
            room.setHotelId(fallbackHotelId);
        }
        if (room.getRoomTypeId() == null && room.getRoomType() != null) {
            room.setRoomTypeId(room.getRoomType().getRoomTypeId());
        }
    }

    private Integer resolveRoomId(Room room, String locationHeader) {
        if (room != null && room.getResolvedId() != null) {
            return room.getResolvedId();
        }
        if (locationHeader == null || locationHeader.isBlank()) {
            return null;
        }
        int lastSlash = locationHeader.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == locationHeader.length() - 1) {
            return null;
        }
        try {
            return Integer.valueOf(locationHeader.substring(lastSlash + 1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Room> loadRoomsFromUrl(String url) throws Exception {
        String raw = rt.getForObject(url, String.class);
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(raw);
        List<Room> rooms = new ArrayList<>();

        if (root.isArray()) {
            rooms.addAll(mapper.convertValue(
                root,
                mapper.getTypeFactory().constructCollectionType(List.class, Room.class)
            ));
        } else if (root.has("_embedded") && root.path("_embedded").has("rooms")) {
            rooms.addAll(mapper.convertValue(
                root.path("_embedded").path("rooms"),
                mapper.getTypeFactory().constructCollectionType(List.class, Room.class)
            ));
        } else if (root.has("content")) {
            rooms.addAll(mapper.convertValue(
                root.path("content"),
                mapper.getTypeFactory().constructCollectionType(List.class, Room.class)
            ));
        }

        return rooms;
    }

    private List<RoomDTO> loadRoomDtosFromUrl(String url) throws Exception {
        String raw = rt.getForObject(url, String.class);
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(raw);
        List<RoomDTO> rooms = new ArrayList<>();

        if (root.isArray()) {
            rooms.addAll(mapper.convertValue(
                root,
                mapper.getTypeFactory().constructCollectionType(List.class, RoomDTO.class)
            ));
        } else if (root.has("_embedded") && root.path("_embedded").has("rooms")) {
            rooms.addAll(mapper.convertValue(
                root.path("_embedded").path("rooms"),
                mapper.getTypeFactory().constructCollectionType(List.class, RoomDTO.class)
            ));
        } else if (root.has("content")) {
            rooms.addAll(mapper.convertValue(
                root.path("content"),
                mapper.getTypeFactory().constructCollectionType(List.class, RoomDTO.class)
            ));
        }

        return rooms;
    }

    private List<Room> loadAndFilterRoomsFallback(int roomTypeId) {
        try {
            List<Room> rooms = loadRoomsFromUrl(baseUrl + "/rooms?size=1000");
            List<Room> filtered = new ArrayList<>();
            for (Room room : rooms) {
                if (room == null) {
                    continue;
                }
                if (room.getRoomTypeId() != null && room.getRoomTypeId().equals(roomTypeId)) {
                    filtered.add(room);
                    continue;
                }
                if (room.getLinks() != null && room.getLinks().getRoomType() != null) {
                    String href = room.getLinks().getRoomType().getHref();
                    if (href != null) {
                        int lastSlash = href.lastIndexOf('/');
                        if (lastSlash > -1 && lastSlash < href.length() - 1
                                && href.substring(lastSlash + 1).equals(String.valueOf(roomTypeId))) {
                            filtered.add(room);
                        }
                    }
                }
            }
            return filtered;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
