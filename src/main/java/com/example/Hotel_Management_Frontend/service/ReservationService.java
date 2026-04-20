package com.example.Hotel_Management_Frontend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.Hotel_Management_Frontend.dto.CreateReservationDTO;
import com.example.Hotel_Management_Frontend.dto.Reservation;
import com.example.Hotel_Management_Frontend.dto.ReservationDetailsDTO;
import com.example.Hotel_Management_Frontend.dto.ReservationPage;
import com.example.Hotel_Management_Frontend.dto.RoomDTO;


@Service
public class ReservationService {

    @Autowired
    private RestTemplate restTemplate;
    private final String baseUrl;

    public ReservationService(@Value("${backend.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }


    public ReservationPage searchReservations(int page,
            String guestName, String email,
            String checkIn, String checkOut) {

        String base = baseUrl;
        String url = "";
        int pageSize = 5;

        // 🔥 decide API based on input

        if (guestName != null && !guestName.isEmpty()) {

            url = base + "/reservations/search/findByGuestNameContainingIgnoreCase"
                    + "?name=" + guestName
                    + "&page=" + page + "&size=" + pageSize;
        }

        else if (email != null && !email.isEmpty()) {

            url = base + "/reservations/search/findByGuestEmailContainingIgnoreCase"
                    + "?email=" + email
                    + "&page=" + page + "&size=" + pageSize;
        }

        else if (checkIn != null && checkOut != null &&
                !checkIn.isEmpty() && !checkOut.isEmpty()) {

            url = base + "/reservations/search/findByCheckInDateBetween"
                    + "?start=" + checkIn + "&end=" + checkOut
                    + "&page=" + page + "&size=" + pageSize;
        }

        else if (checkIn != null && !checkIn.isEmpty()) {

            url = base + "/reservations/search/findByCheckInDate"
                    + "?date=" + checkIn
                    + "&page=" + page + "&size=" + pageSize;
        }

        else if (checkOut != null && !checkOut.isEmpty()) {

            url = base + "/reservations/search/findByCheckOutDate"
                    + "?date=" + checkOut
                    + "&page=" + page + "&size=" + pageSize;
        }

        else {
            // default
            url = base + "/reservations?page=" + page + "&size=" + pageSize;
        }

        // 🔥 CALL API
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        Map<String, Object> pageInfo = (Map<String, Object>) response.get("page");

        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");

        List<Map<String, Object>> list = embedded != null
                ? (List<Map<String, Object>>) embedded.get("reservations")
                : null;

        List<Reservation> finalList = new ArrayList<>();

        if (list != null) {
            for (Map<String, Object> res : list) {
                Reservation r = new Reservation();

                r.setId(((Number) res.get("reservation_id")).intValue());
                r.setGuestName((String) res.get("guestName"));
                r.setGuestEmail((String) res.get("guestEmail"));
                r.setCheckInDate(LocalDate.parse((String) res.get("checkInDate")));
                r.setCheckOutDate(LocalDate.parse((String) res.get("checkOutDate")));

                finalList.add(r);
            }
        }

        int totalPages = 1;
        long totalElements = finalList.size();
        if (pageInfo != null) {
            Object tp = pageInfo.get("totalPages");
            Object te = pageInfo.get("totalElements");
            if (tp instanceof Number) {
                totalPages = ((Number) tp).intValue();
            }
            if (te instanceof Number) {
                totalElements = ((Number) te).longValue();
            }
        }

        ReservationPage result = new ReservationPage();
        result.setReservations(finalList);
        result.setTotalPages(totalPages);
        result.setTotalElements(totalElements);
        result.setPageSize(pageSize);
        return result;
    }

    public ReservationDetailsDTO getReservationDetails(int id) {

        String url = baseUrl + "/api/reservations/" + id + "/details";

        return restTemplate.getForObject(url, ReservationDetailsDTO.class);
    }

    public void addReservation(CreateReservationDTO reservation) {

        String url = baseUrl + "/api/reservations";

        List<Integer> availableRoomIds = new ArrayList<>(Arrays.asList(
                1, 3, 5, 6, 8, 10, 11, 13, 15, 17, 18, 20, 22, 23, 25,
                27, 28, 30, 31, 33, 35, 37, 38, 40, 42, 43, 45, 47, 48, 50, 51));
        Random random = new Random();
        int randomRoomId = availableRoomIds.get(random.nextInt(availableRoomIds.size()));
        RoomDTO room = new RoomDTO();
        room.setRoomId(randomRoomId);
        reservation.setRoom(room);
        System.out.println(reservation);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateReservationDTO> request = new HttpEntity<>(reservation, headers);

        restTemplate.postForObject(url, request, Object.class);
    }
    
    public void deleteReservation(int id) {
    String url = baseUrl + "/reservations/" + id;

    restTemplate.delete(url);
    }

}
