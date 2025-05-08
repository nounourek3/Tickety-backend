package com.example.tickety.controllers;

import com.example.tickety.services.EmailReaderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class EmailTriggerController {

    private final EmailReaderService service;

    public EmailTriggerController(EmailReaderService service) {
        this.service = service;
    }

    @GetMapping("/check")
    public String forceCheck() {
        service.checkInbox();
        return "✅ checkInbox() triggered manually!";
    }
}
