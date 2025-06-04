package com.example.tickety;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class TicketyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketyApplication.class, args);
	}

}
