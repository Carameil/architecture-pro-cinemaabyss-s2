package com.cinemaabyss.events.service;

import com.cinemaabyss.events.model.MovieEvent;
import com.cinemaabyss.events.model.PaymentEvent;
import com.cinemaabyss.events.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishUserEvent(UserEvent event) {
        String eventId = UUID.randomUUID().toString();
        event.setEventId(eventId);
        
        kafkaTemplate.send("user-events", eventId, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published user event: {}", event);
                    } else {
                        log.error("Failed to publish user event: {}", event, ex);
                    }
                });
    }
    
    public void publishPaymentEvent(PaymentEvent event) {
        String eventId = UUID.randomUUID().toString();
        event.setEventId(eventId);
        
        kafkaTemplate.send("payment-events", eventId, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published payment event: {}", event);
                    } else {
                        log.error("Failed to publish payment event: {}", event, ex);
                    }
                });
    }
    
    public void publishMovieEvent(MovieEvent event) {
        String eventId = UUID.randomUUID().toString();
        event.setEventId(eventId);
        
        kafkaTemplate.send("movie-events", eventId, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published movie event: {}", event);
                    } else {
                        log.error("Failed to publish movie event: {}", event, ex);
                    }
                });
    }
} 