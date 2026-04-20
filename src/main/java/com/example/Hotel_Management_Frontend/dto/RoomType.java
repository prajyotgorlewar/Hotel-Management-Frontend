package com.example.Hotel_Management_Frontend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RoomType {

    @JsonProperty("roomTypeId")
    private Integer roomTypeId;

    private String typeName;
    private String description;
    private Integer maxOccupancy;
    private BigDecimal pricePerNight;

    @JsonProperty("_links")
    private Links links;

    @Data
    public static class Links {
        private Link self;
    }

    @Data
    public static class Link {
        private String href;
    }

    public Integer getResolvedId() {
        if (roomTypeId != null) {
            return roomTypeId;
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
