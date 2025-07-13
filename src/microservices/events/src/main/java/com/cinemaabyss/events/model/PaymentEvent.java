package com.cinemaabyss.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentEvent extends Event {
    private Long paymentId;
    private Long userId;
    private BigDecimal amount;
    private String status;
    
    public PaymentEvent(String eventId, Long paymentId, Long userId, BigDecimal amount, String status) {
        super(eventId, java.time.LocalDateTime.now(), "PAYMENT_EVENT");
        this.paymentId = paymentId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }
} 