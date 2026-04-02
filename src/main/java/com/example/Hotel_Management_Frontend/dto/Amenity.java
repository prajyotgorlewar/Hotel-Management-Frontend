package com.example.Hotel_Management_Frontend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Amenity {
    private int amenityId;
    private String name;
    private String description;
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

    public String getSelfHref() {
        if (links == null || links.getSelf() == null) {
            return null;
        }
        return links.getSelf().getHref();
    }
}
