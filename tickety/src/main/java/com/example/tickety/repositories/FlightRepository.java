package com.example.tickety.repositories;

import com.example.tickety.entities.Flight;
import com.example.tickety.entities.FlightEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

    public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByUserId(Long userId);

    boolean existsByFlightEmail(FlightEmail email);
}
