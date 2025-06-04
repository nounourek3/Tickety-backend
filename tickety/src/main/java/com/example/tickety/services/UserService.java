package com.example.tickety.services;

import com.example.tickety.entities.User;
import com.example.tickety.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public List<String> getWishlistCountries(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return new ArrayList<>(user.getWishlistCountries());
    }



    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }


    // Save a new user
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Delete a user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
    public void updateWishlist(Long userId, List<String> wishlist) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setWishlistCountries(wishlist);
        userRepository.save(user);
    }

    public List<String> getWishlist(Long userId) {
        return userRepository.findById(userId)
                .map(User::getWishlistCountries)
                .orElse(Collections.emptyList());
    }

}

