package com.example.tickety.controllers;

import com.example.tickety.DTOS.CreateFlightRequest;
import com.example.tickety.DTOS.Trip;
import com.example.tickety.entities.Flight;
import com.example.tickety.entities.User;
import com.example.tickety.repositories.FlightRepository;
import com.example.tickety.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ POST endpoint for creating flights manually
    @PostMapping
    public ResponseEntity<?> createFlight(@RequestBody CreateFlightRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
            }

            Flight flight = new Flight();
            flight.setUser(userOpt.get());
            flight.setFlightNumber(request.getFlightNumber());
            flight.setBookingCode(request.getBookingCode());
            flight.setDepartureAirport(request.getDepartureAirport());
            flight.setArrivalAirport(request.getArrivalAirport());
            flight.setDepartureTime(request.getDepartureTime());
            flight.setArrivalTime(request.getArrivalTime());

            flight.setFlightDate(LocalDate.parse(request.getFlightDate()));


            Flight saved = flightRepository.save(flight);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving flight: " + e.getMessage());
        }
    }

    // ✅ GET endpoint for retrieving all flights by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Trip>> getFlightsByUser(@PathVariable Long userId) {
        List<Flight> flights = flightRepository.findByUserId(userId);
        List<Trip> trips = flights.stream().map(flight -> {
            Trip trip = new Trip();
            trip.setId(flight.getId()); // ✅ <--- This line is critical

            trip.setFlightNumber(flight.getFlightNumber());
            trip.setFlightDate(flight.getFlightDate());
            trip.setDepartureTime(flight.getDepartureTime());
            trip.setArrivalTime(flight.getArrivalTime());
            trip.setDepartureAirport(flight.getDepartureAirport());
            trip.setArrivalAirport(flight.getArrivalAirport());
            trip.setBookingCode(flight.getBookingCode());
            trip.setSeat(flight.getSeat());
            trip.setAirline(flight.getAirline());

            return trip;
        }).toList();
        return ResponseEntity.ok(trips);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlight(@PathVariable Long id, @RequestBody CreateFlightRequest request) {
        Optional<Flight> flightOpt = flightRepository.findById(id);
        if (flightOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flight not found");
        }

        Flight flight = flightOpt.get(); // ✅ get the existing flight to preserve ID

        // Update its fields
        flight.setFlightNumber(request.getFlightNumber());
        flight.setBookingCode(request.getBookingCode());
        flight.setDepartureAirport(request.getDepartureAirport());
        flight.setArrivalAirport(request.getArrivalAirport());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        try {
            flight.setFlightDate(LocalDate.parse(request.getFlightDate()));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format: " + request.getFlightDate());
        }


        flight.setAirline(request.getAirline());
        flight.setSeat(request.getSeat());

        // Also update user if provided
        if (request.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            userOpt.ifPresent(flight::setUser);
        }

        return ResponseEntity.ok(flightRepository.save(flight)); // ✅ ID preserved
    }


}

