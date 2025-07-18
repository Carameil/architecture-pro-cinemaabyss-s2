package com.cinemaabyss.events.consumer;

import com.cinemaabyss.events.model.MovieEvent;
import com.cinemaabyss.events.model.PaymentEvent;
import com.cinemaabyss.events.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventConsumer {
    
    @KafkaListener(topics = "user-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvent(UserEvent event) {
        log.info("=== CONSUMED USER EVENT ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("User ID: {}", event.getUserId());
        log.info("Action: {}", event.getAction());
        log.info("Details: {}", event.getDetails());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("========================");
    }
    
    @KafkaListener(topics = "payment-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("=== CONSUMED PAYMENT EVENT ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Payment ID: {}", event.getPaymentId());
        log.info("User ID: {}", event.getUserId());
        log.info("Amount: {}", event.getAmount());
        log.info("Status: {}", event.getStatus());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("============================");
    }
    
    @KafkaListener(topics = "movie-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMovieEvent(MovieEvent event) {
        log.info("=== CONSUMED MOVIE EVENT ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Movie ID: {}", event.getMovieId());
        log.info("Title: {}", event.getTitle());
        log.info("Action: {}", event.getAction());
        log.info("User ID: {}", event.getUserId());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("==========================");
    }
} 