package com.example.tickety.controllers;

import com.example.tickety.DTOS.Trip;
import com.example.tickety.entities.FlightEmail;
import com.example.tickety.repositories.FlightEmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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

    //Get all emails by user Id (used in "Mis viajes" pages)
    @GetMapping("/user/{userId}")
    public List<Trip> getEmailByUser(@PathVariable Long userId) {
        return flightEmailRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private Trip mapToDTO(FlightEmail email){
        Trip dto = new Trip();
        dto.subject = email.getSubject();
        dto.sender = email.getSender();
        dto.flightNumber = email.getFlightNumber();
        dto.flightDate = email.getFlightDate();
        dto.departureTime = email.getDepartureTime();
        dto.arrivalTime = email.getArrivalTime();
        dto.departureAirport = email.getDepartureAirport();
        dto.arrivalAirport = email.getArrivalAirport();
        dto.bookingCode = email.getBookingCode();
        return dto;

    }
}
