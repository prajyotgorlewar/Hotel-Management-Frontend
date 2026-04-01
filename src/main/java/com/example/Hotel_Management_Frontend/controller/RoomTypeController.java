package com.example.Hotel_Management_Frontend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Hotel_Management_Frontend.service.RoomTypeService;

@RestController
@RequestMapping("/api/roomtypes")
public class RoomTypeController {

    @Autowired
    private RoomTypeService service;

    @GetMapping
    public String getAll(@RequestParam(defaultValue = "100") int size) {
        return service.getAllRoomTypes(size);
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable String id) {
        return service.getRoomTypeById(id);
    }

    @PostMapping
    public String create(@RequestBody String body) {
        return service.createRoomType(body);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable String id, @RequestBody String body) {
        return service.updateRoomType(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return service.deleteRoomType(id);
    }
}