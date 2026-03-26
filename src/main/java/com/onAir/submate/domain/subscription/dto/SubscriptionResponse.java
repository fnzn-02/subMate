package com.onAir.submate.domain.subscription.dto;

import com.onAir.submate.domain.subscription.entity.Category;
import com.onAir.submate.domain.subscription.entity.PaymentCycle;
import com.onAir.submate.domain.subscription.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id,
        String serviceName,
        BigDecimal price,
        PaymentCycle paymentCycle,
        LocalDate nextPaymentDate,
        boolean isFreeTrial,
        Category category,
        Integer sortOrder,
        LocalDateTime createdAt
) {
    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getServiceName(),
                s.getPrice(),
                s.getPaymentCycle(),
                s.getNextPaymentDate(),
                s.isFreeTrial(),
                s.getCategory(),
                s.getSortOrder(),
                s.getCreatedAt()
        );
    }
}
