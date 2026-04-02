
package com.example.Hotel_Management_Frontend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class RoomTypeDTO1 {


    private Integer roomTypeId;
    private String typeName;
    private String description;
    private int maxOccupancy;
    private BigDecimal pricePerNight;
    private List<RoomDTO> rooms;

}
