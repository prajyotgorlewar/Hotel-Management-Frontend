package com.example.Hotel_Management_Frontend.dto;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Hotel {
    @JsonProperty("hotelId")
    private Integer id;
    private String name;
    private String location;
    private String description;
    private String image;
    private List<Amenity> amenities;
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
        if (id != null) {
            return id;
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
