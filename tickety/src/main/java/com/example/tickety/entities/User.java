package com.example.tickety.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String username;
    private String password;

    @Column(name= "forward_email")
    private String forwardEmail;

    @OneToMany(mappedBy = "user")
    private List<FlightEmail> flightEmails;
    public User() {}

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFlightEmails(List<FlightEmail> flightEmails) {
        this.flightEmails = flightEmails;
    }



}
