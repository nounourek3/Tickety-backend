package com.example.tickety.entities;

import com.example.tickety.entities.FlightEmail;
import jakarta.persistence.*;

@Entity
public class FlightDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String airline;
    private String classType;
    private String seatNumber;

    @ManyToOne
    @JoinColumn(name="flight_email_id")
    private FlightEmail flightEmail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public FlightEmail getFlightEmail() {
        return flightEmail;
    }

    public void setFlightEmail(FlightEmail flightEmail) {
        this.flightEmail = flightEmail;
    }
}
