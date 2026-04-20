package com.example.Hotel_Management_Frontend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RoomTypeResponse {

    @JsonProperty("_embedded")
    private Embedded embedded;

    private Page page;

    @Data
    public static class Embedded {
        private List<RoomType> roomTypes;
    }

    @Data
    public static class Page {
        private int totalPages;
        private long totalElements;
        private int number;
        private int size;
    }
}
