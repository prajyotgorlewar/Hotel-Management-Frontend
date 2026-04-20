package com.example.Hotel_Management_Frontend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RoomResponse {

    @JsonProperty("_embedded")
    private Embedded embedded;

    private Page page;

    @Data
    public static class Embedded {
        private List<Room> rooms;
    }

    @Data
    public static class Page {
        private int totalPages;
        private long totalElements;
        private int number;
        private int size;
    }
}
