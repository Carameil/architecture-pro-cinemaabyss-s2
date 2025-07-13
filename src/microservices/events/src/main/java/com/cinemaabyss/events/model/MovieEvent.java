package com.cinemaabyss.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MovieEvent extends Event {
    private Long movieId;
    private String title;
    private String action;
    private Long userId;
    
    public MovieEvent(String eventId, Long movieId, String title, String action, Long userId) {
        super(eventId, java.time.LocalDateTime.now(), "MOVIE_EVENT");
        this.movieId = movieId;
        this.title = title;
        this.action = action;
        this.userId = userId;
    }
} 