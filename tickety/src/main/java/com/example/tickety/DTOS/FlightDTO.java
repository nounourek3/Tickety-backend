package com.example.tickety.DTOS;

import com.example.tickety.entities.Flight;

import java.util.Date;

public class FlightDTO {
    private Long id;
    private String flightNumber;
    private String bookingCode;
    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String arrivalTime;
    private Date flightDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Date getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(Date flightDate) {
        this.flightDate = flightDate;
    }

    public FlightDTO(Flight flight) {
        this.id = flight.getId();
        this.flightNumber = flight.getFlightNumber();
        this.bookingCode = flight.getBookingCode();
        this.departureAirport = flight.getDepartureAirport();
        this.arrivalAirport = flight.getArrivalAirport();
        this.departureTime = flight.getDepartureTime();
        this.arrivalTime = flight.getArrivalTime();
        this.flightDate = flight.getFlightDate();
    }


    // Getters and setters omitted for brevity
}

