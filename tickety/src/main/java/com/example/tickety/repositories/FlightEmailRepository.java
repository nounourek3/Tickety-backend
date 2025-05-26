package com.example.tickety.repositories;

import com.example.tickety.entities.FlightEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightEmailRepository extends JpaRepository<FlightEmail, Long> {
    List<FlightEmail> findByUserId(Long userId);


}

