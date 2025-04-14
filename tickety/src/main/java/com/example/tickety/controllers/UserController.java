package com.example.tickety.controllers;

import com.example.tickety.entities.User;
import com.example.tickety.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getUser (@PathVariable Long id){
        User user = userService.getUserById(id).orElse(null);
        System.out.println(user);  // Debugging line to see the user
        return user;

    }
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user); // Use the service, not the repo
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}
