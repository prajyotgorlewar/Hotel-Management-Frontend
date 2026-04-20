package com.example.Hotel_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Room {

    @JsonProperty("roomId")
    @JsonAlias({"room_id"})
    private Integer roomId;

    @JsonAlias({"room_number"})
    private Integer roomNumber;

    @JsonAlias({"is_available"})
    private Boolean isAvailable;

    @JsonAlias({"room_type_id", "roomTypeId"})
    private Integer roomTypeId;

    @JsonAlias({"hotel_id", "hotelId"})
    private Integer hotelId;

    @JsonProperty("_links")
    private Links links;

    @Data
    public static class Links {
        private Link self;
        private Link roomType;
    }

    @Data
    public static class Link {
        private String href;
    }

    public Integer getResolvedId() {
        if (roomId != null) {
            return roomId;
        }
        if (links == null || links.getSelf() == null || links.getSelf().getHref() == null) {
            return null;
        }
        String href = links.getSelf().getHref();
        int lastSlash = href.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == href.length() - 1) {
            return null;
        }
        try {
            return Integer.valueOf(href.substring(lastSlash + 1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
