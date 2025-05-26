package com.example.tickety.controllers;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.example.tickety.DTOS.ConfirmedFlight;
import com.example.tickety.DTOS.FlightDTO;
import com.example.tickety.DTOS.Trip;
import com.example.tickety.entities.Flight;
import com.example.tickety.entities.FlightEmail;
import com.example.tickety.entities.User;
import com.example.tickety.repositories.FlightEmailRepository;
import com.example.tickety.repositories.FlightRepository;
import com.example.tickety.services.FlightParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/trips")

public class FlightEmailController {
    @Autowired
    private FlightParserService flightParserService;
    @Autowired
    private FlightEmailRepository flightEmailRepository;
    @Autowired
    private FlightRepository flightRepository;

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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/emails/user/{userId}")
    public List<FlightEmail> getUnconfirmedEmailsByUser(@PathVariable Long userId) {
        return flightEmailRepository.findByUserId(userId).stream()
                .filter(email ->
                        (isNullOrEmpty(email.getFlightNumber()) ||
                                isNullOrEmpty(email.getBookingCode()) ||
                                isNullOrEmpty(email.getDepartureAirport()) ||
                                isNullOrEmpty(email.getArrivalAirport()) ||
                                email.getFlightDate() == null ||
                                isNullOrEmpty(email.getDepartureTime()) ||
                                isNullOrEmpty(email.getArrivalTime()))
                                && !flightRepository.existsByFlightEmail(email) // ✅ excludes saved
                )
                .collect(Collectors.toList());
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }








    @GetMapping("/parse/{flightEmailId}")
    public ResponseEntity<?> parseFlightEmail(@PathVariable Long flightEmailId) {
        return flightEmailRepository.findById(flightEmailId)
                .map(email -> {
                    var result = flightParserService.extractFlightInfo(
                            email.getRawText(),
                            email.getSubject(),
                            email.getCreatedAt() != null ? email.getCreatedAt() : new java.util.Date()
                    );
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    private Trip mapToDTO(FlightEmail email) {
        Trip dto = new Trip();

        dto.setSubject(email.getSubject() != null ? email.getSubject() : "");
        dto.setSender(email.getSender() != null ? email.getSender() : "");
        dto.setFlightNumber(email.getFlightNumber() != null ? email.getFlightNumber() : "");
        dto.setDepartureTime(email.getDepartureTime());
        dto.setArrivalTime(email.getArrivalTime());
        dto.setDepartureAirport(email.getDepartureAirport() != null ? email.getDepartureAirport() : "");
        dto.setArrivalAirport(email.getArrivalAirport() != null ? email.getArrivalAirport() : "");
        dto.setBookingCode(email.getBookingCode() != null ? email.getBookingCode() : "");

        // 👇 Convert partial date (day + month) to java.util.Date if possible
        if (email.getFlightDate() != null) {
            dto.setFlightDate(email.getFlightDate());
        } else if (email.getFlightDay() != null && email.getFlightMonth() != null) {
            try {
                int year = LocalDate.now().getYear(); // or choose a fallback strategy
                int month = Integer.parseInt(email.getFlightMonth());
                int day = Integer.parseInt(email.getFlightDay());

                LocalDate localDate = LocalDate.of(year, month, day);
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                dto.setFlightDate(date);
            } catch (Exception e) {
                System.out.println("⚠️ Fecha inválida ignorada: " + e.getMessage());
                dto.setFlightDate(null);
            }
        } else {
            dto.setFlightDate(null);
        }

        return dto;
    }


    @PostMapping("/flights")
    public ResponseEntity<?> saveFlight(@RequestBody ConfirmedFlight data) {
        System.out.println("📩 [saveFlight] Request received with:");
        System.out.println("➡️ userId: " + data.getUserId());
        System.out.println("➡️ flightEmailId: " + data.getFlightEmailId());
        System.out.println("➡️ flightNumber: " + data.getFlightNumber());

        Optional<FlightEmail> emailOpt = flightEmailRepository.findById(data.getFlightEmailId());
        if (emailOpt.isEmpty()) {
            System.out.println("❌ FlightEmail not found for ID: " + data.getFlightEmailId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flight email not found.");
        }

        FlightEmail email = emailOpt.get();
        System.out.println("📬 Found email with subject: " + email.getSubject());

        // 🚨 Check for duplicate flight already saved with this email
        boolean alreadyExists = flightRepository.existsByFlightEmail(email);
        System.out.println("🧪 Checking if flight already exists for this email → " + alreadyExists);

        if (alreadyExists) {
            System.out.println("⚠️ Flight already exists for this email. Skipping save.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Flight already saved for this email.");
        }

        // ✅ Create new flight object
        Flight flight = new Flight();
        flight.setFlightEmail(email);

        User user = new User();
        user.setId(data.getUserId());
        flight.setUser(user);

        flight.setFlightNumber(data.getFlightNumber());
        flight.setBookingCode(data.getBookingCode());
        flight.setDepartureAirport(data.getDepartureAirport());
        flight.setArrivalAirport(data.getArrivalAirport());
        flight.setDepartureTime(data.getDepartureTime());
        flight.setArrivalTime(data.getArrivalTime());
        flight.setFlightDate(data.getFlightDate());

        Flight savedFlight = flightRepository.save(flight);
        System.out.println("✅ Flight saved successfully with ID: " + savedFlight.getId());

        return ResponseEntity.ok(Map.of("message", "Vuelo guardado"));
    }





    @GetMapping("/flights/user/{userId}")
    public List<FlightDTO> getFlightsByUser(@PathVariable Long userId) {
        return flightRepository.findByUserId(userId).stream()
                .map(FlightDTO::new)
                .toList();
    }


}
