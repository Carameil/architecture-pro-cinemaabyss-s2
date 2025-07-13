package com.cinemaabyss.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserEvent extends Event {
    private Long userId;
    private String action;
    private String details;
    
    public UserEvent(String eventId, Long userId, String action, String details) {
        super(eventId, java.time.LocalDateTime.now(), "USER_EVENT");
        this.userId = userId;
        this.action = action;
        this.details = details;
    }
} 