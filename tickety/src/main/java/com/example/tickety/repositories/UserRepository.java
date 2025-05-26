package com.example.tickety.repositories;

import com.example.tickety.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByForwardEmail(String forwardEmail);

    Optional<User> findByEmail(String email);
}
