package com.example.tickety.entities;

import jakarta.persistence.*;

@Entity
public class BoardingPass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String qrCodeUrl;

    @ManyToOne
    @JoinColumn(name = "flight_email_id")
    private FlightEmail flightEmail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public FlightEmail getFlightEmail() {
        return flightEmail;
    }

    public void setFlightEmail(FlightEmail flightEmail) {
        this.flightEmail = flightEmail;
    }
}
