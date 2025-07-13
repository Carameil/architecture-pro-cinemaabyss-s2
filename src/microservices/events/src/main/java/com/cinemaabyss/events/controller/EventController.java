package com.cinemaabyss.events.controller;

import com.cinemaabyss.events.model.MovieEvent;
import com.cinemaabyss.events.model.PaymentEvent;
import com.cinemaabyss.events.model.UserEvent;
import com.cinemaabyss.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "events");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> createUserEvent(@RequestBody Map<String, Object> request) {
        UserEvent event = new UserEvent(
                null,
                Long.parseLong(request.getOrDefault("userId", "1").toString()),
                request.getOrDefault("action", "USER_ACTION").toString(),
                request.getOrDefault("details", "User event details").toString()
        );
        
        eventService.publishUserEvent(event);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User event created");
        response.put("eventId", event.getEventId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> createPaymentEvent(@RequestBody Map<String, Object> request) {
        PaymentEvent event = new PaymentEvent(
                null,
                Long.parseLong(request.getOrDefault("paymentId", "1").toString()),
                Long.parseLong(request.getOrDefault("userId", "1").toString()),
                new java.math.BigDecimal(request.getOrDefault("amount", "100.00").toString()),
                request.getOrDefault("status", "COMPLETED").toString()
        );
        
        eventService.publishPaymentEvent(event);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment event created");
        response.put("eventId", event.getEventId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/movie")
    public ResponseEntity<Map<String, Object>> createMovieEvent(@RequestBody Map<String, Object> request) {
        MovieEvent event = new MovieEvent(
                null,
                Long.parseLong(request.getOrDefault("movieId", "1").toString()),
                request.getOrDefault("title", "Movie Title").toString(),
                request.getOrDefault("action", "MOVIE_VIEWED").toString(),
                Long.parseLong(request.getOrDefault("userId", "1").toString())
        );
        
        eventService.publishMovieEvent(event);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Movie event created");
        response.put("eventId", event.getEventId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 