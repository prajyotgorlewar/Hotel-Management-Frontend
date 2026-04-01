package com.example.Hotel_Management_Frontend.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
          //  .registeredModules(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Value("${backend.base-url}")
    private String backendUrl;

    private static final int PAGE_SIZE = 6;

    // ─────────────────────────────────────────
    // GET /reviews?hotelId=1&page=0
    // ─────────────────────────────────────────
    @GetMapping
    public String listReviews(
            @RequestParam Integer hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String filterBy,
            @RequestParam(required = false) String keyword,
            Model model) {

        String url = backendUrl + "/review/search/findDistinctByReservationRoomHotelHotelId?hotelId=" + hotelId
                + "&projection=reviewDto";

        try {
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);
            JsonNode embedded = root.path("_embedded").path("reviews");

            List<Map<String, Object>> allReviews = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("review_id",   node.path("review_id").asInt());
                    r.put("comment",     node.path("comment").asText(""));
                    r.put("rating",      node.path("rating").asInt());
                    r.put("review_date", node.path("review_date").asText(""));
                    // Truncate comment to first 6–8 words for the list view
                    String fullComment = node.path("comment").asText("");
                    String[] words = fullComment.split("\\s+");
                    String shortComment = String.join(" ", Arrays.copyOfRange(words, 0, Math.min(7, words.length)));
                    if (words.length > 7) shortComment += "…";
                    r.put("shortComment", shortComment);
                    allReviews.add(r);
                }
            }
            // 🔍 FILTER LOGIC
            if (keyword != null && !keyword.isEmpty() &&
                    filterBy != null && !filterBy.isEmpty()) {

                String key = keyword.toLowerCase().trim();

                allReviews = allReviews.stream().filter(r -> {
                    switch (filterBy) {

                        case "comment":
                            return r.get("comment").toString().toLowerCase().contains(key);

                        case "rating":
                            return String.valueOf(r.get("rating")).trim().equals(key);

                        case "date":
                            return r.get("review_date").toString().contains(key);

                        default:
                            return true;
                    }
                }).collect(java.util.stream.Collectors.toList());
            }

            // Paginate in-memory
            int total = allReviews.size();
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
            int from = page * PAGE_SIZE;
            int to = Math.min(from + PAGE_SIZE, total);
            List<Map<String, Object>> paged = (from < total) ? allReviews.subList(from, to) : Collections.emptyList();

            model.addAttribute("reviews", paged);
            model.addAttribute("hotelId", hotelId);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);

        } catch (Exception e) {
            model.addAttribute("message", "Unable to load reviews: " + e.getMessage());
            model.addAttribute("reviews", Collections.emptyList());
            model.addAttribute("hotelId", hotelId);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
        }
        model.addAttribute("filterBy", filterBy);
        model.addAttribute("keyword", keyword);
        return "review/reviews";
    }

    // ─────────────────────────────────────────
    // GET /reviews/{id}?hotelId=1
    // ─────────────────────────────────────────
    @GetMapping("/{reviewId}")
    public String reviewDetail(
            @PathVariable Integer reviewId,
            @RequestParam Integer hotelId,
            Model model) {

        String url = backendUrl + "/review/" + reviewId + "?projection=reviewDetails";

        try {
            String json = restTemplate.getForObject(url, String.class);
            JsonNode node = mapper.readTree(json);

            Map<String, Object> review = new LinkedHashMap<>();
            review.put("review_id",                 reviewId);
            review.put("comment",                   node.path("comment").asText(""));
            review.put("rating",                    node.path("rating").asInt());
            review.put("review_date",               node.path("review_date").asText(""));
            JsonNode reservation = node.path("_embedded").path("reservation");

            review.put("reservation_GuestName",
                    reservation.path("guestName").asText("—"));

            review.put("reservation_CheckInDate",
                    reservation.path("checkInDate").asText("—"));

            review.put("reservation_CheckOutDate",
                    reservation.path("checkOutDate").asText("—"));
            int reservationId =
                    reservation.path("reservation_id").asInt(
                            reservation.path("reservationId").asInt(
                                    reservation.path("id").asInt(-1)));

            review.put("reservation_Reservation_id", reservationId);
            /*
            review.put("reservation_Reservation_id",
                    reservation.path("reservation_id").asInt());*/
            model.addAttribute("review", review);
            model.addAttribute("hotelId", hotelId);

        } catch (Exception e) {
            model.addAttribute("message", "Unable to load review details: " + e.getMessage());
        }

        return "review/reviewdetails";
    }

    // ─────────────────────────────────────────
    // POST /reviews/create  (INSERT)
    // ─────────────────────────────────────────
    @PostMapping("/create")
    public String createReview(
            @RequestParam Integer hotelId,
            @RequestParam Integer reservationId,
            @RequestParam String comment,
            @RequestParam int rating,
            @RequestParam String review_date,
            Model model) {

        String url = backendUrl + "/review";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("comment", comment);
        body.put("rating", rating);
        body.put("review_date", review_date);
        body.put("reservation", backendUrl + "/reservation/" + reservationId);

        try {
            restTemplate.postForObject(url, body, String.class);
            return "redirect:/reviews?hotelId=" + hotelId + "&message=Review+submitted+successfully";
        } catch (Exception e) {
            return "redirect:/reviews?hotelId=" + hotelId + "&message=Error+submitting+review";
        }
    }

    // ─────────────────────────────────────────
    // POST /reviews/{id}/update  (PUT)
    // ─────────────────────────────────────────
    @PostMapping("/{reviewId}/update")
    public String updateReview(
            @PathVariable Integer reviewId,
            @RequestParam Integer hotelId,
            @RequestParam String comment,
            @RequestParam int rating,
            @RequestParam String review_date) {

        String url = backendUrl + "/review/" + reviewId;

        // Fetch existing reservation link first so we don't lose it
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("comment", comment);
        body.put("rating", rating);
        body.put("review_date", review_date);

        try {
            restTemplate.put(url, body);
            return "redirect:/reviews?hotelId=" + hotelId + "&message=Review+updated+successfully";
        } catch (Exception e) {
            return "redirect:/reviews?hotelId=" + hotelId + "&message=Error+updating+review";
        }
    }
}

