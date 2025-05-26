package com.example.tickety.controllers;


import com.example.tickety.DTOS.CreateFlightRequest;
import com.example.tickety.entities.Flight;
import com.example.tickety.entities.FlightEmail;
import com.example.tickety.entities.User;
import com.example.tickety.repositories.FlightEmailRepository;
import com.example.tickety.repositories.FlightRepository;
import com.example.tickety.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping
public class FlightController {
    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightEmailRepository flightEmailRepository;

    @PostMapping
    public ResponseEntity<?> createFlight(@RequestBody CreateFlightRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            Optional<FlightEmail> emailOpt = flightEmailRepository.findById(request.getFlightEmailId());

            if (userOpt.isEmpty() || emailOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User or FlightEmail not found");
            }

            Flight flight = new Flight();
            flight.setUser(userOpt.get());
            flight.setFlightEmail(emailOpt.get());
            flight.setFlightNumber(request.getFlightNumber());
            flight.setBookingCode(request.getBookingCode());
            flight.setDepartureAirport(request.getDepartureAirport());
            flight.setArrivalAirport(request.getArrivalAirport());
            flight.setDepartureTime(request.getDepartureTime());
            flight.setArrivalTime(request.getArrivalTime());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            flight.setFlightDate(formatter.parse(request.getFlightDate()));

            Flight saved = flightRepository.save(flight);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving flight: " + e.getMessage());
        }
    }
}
