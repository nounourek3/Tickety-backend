package com.example.tickety.controllers;

import com.example.tickety.config.PasswordEncoderConfig;
import com.example.tickety.entities.User;
import com.example.tickety.repositories.UserRepository;
import com.example.tickety.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    public UserRepository userRepository;
    @Autowired
    private UserService userService;

    private final PasswordEncoder passwordEncoder;

    public UserController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }



    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers(); // You’ll need to implement this method
    }


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
    @PostMapping("/profile-picture/{userId}")
    public ResponseEntity<?> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Save file to /uploads/profile_pics/
            String uploadDir = "uploads/profile_pics/";
            String filename = "user_" + userId + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();

            Files.createDirectories(Paths.get(uploadDir)); // Asegúrate de que exista la carpeta
            try {
                Files.write(filePath, file.getBytes());
                System.out.println("✅ File written to: " + filePath.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("❌ Failed to write file: " + e.getMessage());
                e.printStackTrace();
            }


            User user = userOpt.get();
            user.setProfileImageUrl("/uploads/profile_pics/" + filename); // ✅ URL pública correcta
            userRepository.save(user);


            return ResponseEntity.ok(Map.of("profileImageUrl", "/uploads/profile_pics/" + filename));



        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }

        if (updates.containsKey("password")) {
            String rawPassword = (String) updates.get("password");
            String hashedPassword = passwordEncoder.encode(rawPassword);
            user.setPassword(hashedPassword);
        }

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{id}/wishlist")
    @Transactional
    public ResponseEntity<Void> updateWishlist(@PathVariable Long id, @RequestBody List<String> wishlist) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setWishlistCountries(wishlist);
        userRepository.save(user); // aunque esté gestionado por JPA, es buena práctica

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @GetMapping("/{userId}/wishlist")
    public ResponseEntity<List<String>> getWishlist(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getWishlist(userId));
    }

}


