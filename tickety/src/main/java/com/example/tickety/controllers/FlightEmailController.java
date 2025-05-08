package com.example.tickety.controllers;

import com.example.tickety.DTOS.Trip;
import com.example.tickety.entities.FlightEmail;
import com.example.tickety.repositories.FlightEmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/trips")
public class FlightEmailController {

    @Autowired
    private FlightEmailRepository flightEmailRepository;

    //Get all flight emails (for testing or admin)
    @GetMapping
    public List<FlightEmail> getAllEmails() {
        return flightEmailRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Trip>> getEmailByUser(@PathVariable Long userId) {
        try {
            List<Trip> trips = flightEmailRepository.findByUserId(userId)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            e.printStackTrace(); // or use log.error("Error mapping trips", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Trip mapToDTO(FlightEmail email) {
        Trip dto = new Trip();

        dto.setSubject(email.getSubject() != null ? email.getSubject() : "");
        dto.setSender(email.getSender() != null ? email.getSender() : "");
        dto.setFlightNumber(email.getFlightNumber() != null ? email.getFlightNumber() : "");
        dto.setFlightDate(email.getFlightDate()); // nullable is OK
        dto.setDepartureTime(email.getDepartureTime());
        dto.setArrivalTime(email.getArrivalTime());
        dto.setDepartureAirport(email.getDepartureAirport() != null ? email.getDepartureAirport() : "");
        dto.setArrivalAirport(email.getArrivalAirport() != null ? email.getArrivalAirport() : "");
        dto.setBookingCode(email.getBookingCode() != null ? email.getBookingCode() : "");

        return dto;
    }


}
