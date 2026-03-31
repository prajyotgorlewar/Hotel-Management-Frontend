package com.example.Hotel_Management_Frontend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.Hotel_Management_Frontend.dto.Reservation;
import com.example.Hotel_Management_Frontend.dto.ReservationDetailsDTO;

@Service
public class ReservationService {

    @Autowired
    private RestTemplate restTemplate;


    public List<Reservation> searchReservations(int page,
            String guestName, String email,
            String checkIn, String checkOut) {

        String base = "http://172.16.160.110:8081";
        String url = "";

        // 🔥 decide API based on input

        if (guestName != null && !guestName.isEmpty()) {

            url = base + "/reservations/search/findByGuestNameContainingIgnoreCase"
                    + "?name=" + guestName
                    + "&page=" + page + "&size=5";
        }

        else if (email != null && !email.isEmpty()) {

            url = base + "/reservations/search/findByGuestEmailContainingIgnoreCase"
                    + "?email=" + email
                    + "&page=" + page + "&size=5";
        }

        else if (checkIn != null && checkOut != null &&
                !checkIn.isEmpty() && !checkOut.isEmpty()) {

            url = base + "/reservations/search/findByCheckInDateBetween"
                    + "?start=" + checkIn + "&end=" + checkOut
                    + "&page=" + page + "&size=5";
        }

        else if (checkIn != null && !checkIn.isEmpty()) {

            url = base + "/reservations/search/findByCheckInDate"
                    + "?date=" + checkIn
                    + "&page=" + page + "&size=5";
        }

        else if (checkOut != null && !checkOut.isEmpty()) {

            url = base + "/reservations/search/findByCheckOutDate"
                    + "?date=" + checkOut
                    + "&page=" + page + "&size=5";
        }

        else {
            // default
            url = base + "/reservations?page=" + page + "&size=5";
        }

        // 🔥 CALL API
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        Map<String, Object> pageInfo = (Map<String, Object>) response.get("page");

        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");

        List<Map<String, Object>> list = (List<Map<String, Object>>) embedded.get("reservations");

        List<Reservation> finalList = new ArrayList<>();

        for (Map<String, Object> res : list) {
            Reservation r = new Reservation();

            r.setId(((Number) res.get("reservation_id")).intValue());
            r.setGuestName((String) res.get("guestName"));
            r.setGuestEmail((String) res.get("guestEmail"));
            r.setCheckInDate((String) res.get("checkInDate"));
            r.setCheckOutDate((String) res.get("checkOutDate"));

            finalList.add(r);
        }

        return finalList;
    }

    public ReservationDetailsDTO getReservationDetails(int id) {

    String url = "http://172.16.160.110:8081/api/reservations/" + id + "/details";

    return restTemplate.getForObject(url, ReservationDetailsDTO.class);
}

}
