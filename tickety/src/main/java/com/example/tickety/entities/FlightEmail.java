package com.example.tickety.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
public class FlightEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String sender;
    private String flightNumber;

    @Temporal(TemporalType.DATE)
    private Date flightDate;

    private String departureTime;
    private String arrivalTime;
    private String departureAirport;
    private String arrivalAirport;
    private String bookingCode;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date(); // auto-timestamp

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "flightEmail")
    private FlightNotification flightNotification;

    // Getters and Setters...

    public void setId(Long id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setFlightDate(Date flightDate) {
        this.flightDate = flightDate;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFlightNotification(FlightNotification flightNotification) {
        this.flightNotification = flightNotification;
    }
}
