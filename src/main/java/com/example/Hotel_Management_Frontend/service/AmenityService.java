package com.example.Hotel_Management_Frontend.service;

import com.example.Hotel_Management_Frontend.dto.AmenityDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class AmenityService {

    @Autowired
    private RestTemplate restTemplate;

    private final String baseUrl;

    public AmenityService(@Value("${backend.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class HalLink { private String href; }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AmenityHal {
        private Integer amenityId;
        private String name;
        private String description;

        @JsonProperty("_links")
        private Map<String, HalLink> links;

        public Integer extractId() {
            if (amenityId != null) return amenityId;
            if (links != null && links.containsKey("self")) {
                String href = links.get("self").getHref();
                String[] parts = href.replaceAll("\\{.*\\}", "").trim().split("/");
                try { return Integer.parseInt(parts[parts.length - 1].trim()); }
                catch (NumberFormatException ignored) {}
            }
            return null;
        }

        public AmenityDTO toDTO() {
            AmenityDTO dto = new AmenityDTO();
            dto.setAmenityId(extractId());
            dto.setName(name);
            dto.setDescription(description);
            return dto;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AmenityEmbedded { private List<AmenityHal> amenities; }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AmenityPage {
        @JsonProperty("_embedded")
        private AmenityEmbedded embedded;
    }

    public List<AmenityDTO> getAmenitiesByRoom(Integer roomId) {
        List<String> urls = List.of(
                baseUrl + "/rooms/" + roomId + "/amenities",
                baseUrl + "/amenities/search/findByRooms_RoomId?roomId=" + roomId,
                baseUrl + "/amenities/search/findByRooms_roomId?roomId=" + roomId,
                baseUrl + "/amenities/search/findByRoomId?roomId=" + roomId,
                baseUrl + "/amenities/search/findByRoomsId?roomId=" + roomId
        );

        for (String url : urls) {
            AmenityPage page = fetchAmenityPage(url);
            if (page == null || page.getEmbedded() == null
                    || page.getEmbedded().getAmenities() == null) {
                continue;
            }
            List<AmenityDTO> result = new ArrayList<>();
            for (AmenityHal a : page.getEmbedded().getAmenities()) {
                result.add(a.toDTO());
            }
            return result;
        }

        return List.of();
    }

    public List<AmenityDTO> getAllAmenities() {
        String url = baseUrl + "/amenities?size=200";
        AmenityPage page = fetchAmenityPage(url);
        if (page == null || page.getEmbedded() == null
                || page.getEmbedded().getAmenities() == null) {
            return List.of();
        }
        List<AmenityDTO> result = new ArrayList<>();
        for (AmenityHal a : page.getEmbedded().getAmenities()) {
            result.add(a.toDTO());
        }
        return result;
    }

    public void createAndAssign(Integer roomId, AmenityDTO dto) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", dto.getName());
        body.put("description", dto.getDescription());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<AmenityHal> resp = postAmenity(body, headers);
        Integer amenityId = null;
        if (resp.getBody() != null) {
            amenityId = resp.getBody().extractId();
        }
        if (amenityId == null && resp.getHeaders().getLocation() != null) {
            String[] parts = UriComponentsBuilder.fromUri(resp.getHeaders().getLocation())
                    .build()
                    .getPath()
                    .split("/");
            amenityId = Integer.parseInt(parts[parts.length - 1]);
        }
        if (amenityId == null) {
            throw new IllegalStateException("Amenity was created, but its id could not be resolved.");
        }
        assignToRoom(roomId, amenityId);
    }

    public void assignToRoom(Integer roomId, Integer amenityId) {
        String amenityUri = baseUrl + "/amenities/" + amenityId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/uri-list"));
        HttpEntity<String> entity = new HttpEntity<>(amenityUri, headers);

        List<String> urls = List.of(
                baseUrl + "/rooms/" + roomId + "/amenities",
                baseUrl + "/api/rooms/" + roomId + "/amenities"
        );
        RuntimeException lastError = null;
        for (String url : urls) {
            try {
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                return;
            } catch (Exception ex) {
                lastError = new RuntimeException(ex.getMessage(), ex);
            }
        }
        if (lastError != null) {
            throw lastError;
        }
    }

    public void updateAmenity(Integer amenityId, AmenityDTO dto) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", dto.getName());
        body.put("description", dto.getDescription());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(baseUrl + "/amenities/" + amenityId,
                HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
    }

    public void unassignFromRoom(Integer roomId, Integer amenityId) {
        restTemplate.delete( baseUrl+ "/rooms/" + roomId + "/amenities/" + amenityId);
    }

    public void deleteAmenity(Integer amenityId) {
        restTemplate.delete(baseUrl + "/amenities/" + amenityId);
    }

    private AmenityPage fetchAmenityPage(String url) {
        try {
            return restTemplate.getForObject(url, AmenityPage.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private ResponseEntity<AmenityHal> postAmenity(Map<String, Object> body, HttpHeaders headers) {
        List<String> urls = List.of(
                baseUrl + "/amenities",
                baseUrl + "/api/amenities"
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        RuntimeException lastError = null;
        for (String url : urls) {
            try {
                return restTemplate.postForEntity(url, entity, AmenityHal.class);
            } catch (HttpStatusCodeException ex) {
                lastError = new RuntimeException(ex.getStatusCode() + " " + ex.getResponseBodyAsString(), ex);
            } catch (Exception ex) {
                lastError = new RuntimeException(ex.getMessage(), ex);
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        throw new IllegalStateException("Amenity creation failed.");
    }
}

