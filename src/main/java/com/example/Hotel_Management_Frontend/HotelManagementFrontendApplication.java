package com.example.Hotel_Management_Frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotelManagementFrontendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelManagementFrontendApplication.class, args);
		System.out.println("Frontend Application is running on port 8082");
	}

}
