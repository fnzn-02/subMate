package com.onAir.submate.domain.subscription.dto;

import com.onAir.submate.domain.subscription.entity.Category;
import com.onAir.submate.domain.subscription.entity.PaymentCycle;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedSubscriptionDto(
        String serviceName,
        BigDecimal price,
        String currency,         // "KRW" or "USD"
        PaymentCycle paymentCycle,
        LocalDate nextPaymentDate,
        Category category
) {}
