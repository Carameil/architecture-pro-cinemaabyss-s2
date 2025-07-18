package com.cinemaabyss.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Event {
    private String eventId;
    private LocalDateTime timestamp;
    private String eventType;
} 