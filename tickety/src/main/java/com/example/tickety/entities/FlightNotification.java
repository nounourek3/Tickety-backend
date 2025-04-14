package com.example.tickety.entities;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class FlightNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date notificationTime;
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(Date notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FlightEmail getFlightEmail() {
        return flightEmail;
    }

    public void setFlightEmail(FlightEmail flightEmail) {
        this.flightEmail = flightEmail;
    }

    @OneToOne
    @JoinColumn(name="flight_email_id")
    private FlightEmail flightEmail;
}
