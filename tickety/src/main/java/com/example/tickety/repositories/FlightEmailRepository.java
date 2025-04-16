package com.example.tickety.repositories;

import com.example.tickety.entities.FlightEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightEmailRepository extends JpaRepository<FlightEmail, Long> {
}
