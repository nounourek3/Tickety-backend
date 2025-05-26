package com.example.tickety.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private User user;

    @OneToOne(mappedBy = "flightEmail")
    private FlightNotification flightNotification;

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getSender() {
        return sender;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public Date getFlightDate() {
        return flightDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    private String pdfUrl;

    public FlightNotification getFlightNotification() {
        return flightNotification;
    }

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

    @Column(name = "flight_day")
    private String flightDay;

    @Column(name = "flight_month")
    private String flightMonth;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getFlightDay() {
        return flightDay;
    }

    public void setFlightDay(String flightDay) {
        this.flightDay = flightDay;
    }

    public String getFlightMonth() {
        return flightMonth;
    }

    public void setFlightMonth(String flightMonth) {
        this.flightMonth = flightMonth;
    }
    public void setFlightNotification(FlightNotification flightNotification) {
        this.flightNotification = flightNotification;
    }

    public void setContent(String content) {
    }
}
