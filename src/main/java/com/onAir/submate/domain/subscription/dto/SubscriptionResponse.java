package com.onAir.submate.domain.subscription.dto;

import com.onAir.submate.domain.exchange.service.ExchangeRateService;
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
        @com.fasterxml.jackson.annotation.JsonProperty("freeTrial") boolean isFreeTrial,
        Category category,
        Integer sortOrder,
        LocalDateTime createdAt,
        String paymentMethod,
        @com.fasterxml.jackson.annotation.JsonProperty("dollarService") boolean isDollarService,
        BigDecimal priceInKrw,
        BigDecimal exchangeRate,
        BigDecimal originalPrice
) {
    public static SubscriptionResponse from(Subscription s) {
        return from(s, null);
    }

    public static SubscriptionResponse from(Subscription s, ExchangeRateService exchangeRateService) {
        LocalDate next = s.getNextPaymentDate();
        LocalDate today = LocalDate.now();
        while (next.isBefore(today)) {
            next = s.getPaymentCycle() == PaymentCycle.YEARLY
                    ? next.plusYears(1)
                    : next.plusMonths(1);
        }

        boolean isDollar = s.isDollar();
        BigDecimal rate = (exchangeRateService != null)
                ? exchangeRateService.getLatestUsdToKrw()
                : null;
        BigDecimal priceInKrw = s.getPrice();

        return new SubscriptionResponse(
                s.getId(),
                s.getServiceName(),
                s.getPrice(),
                s.getPaymentCycle(),
                next,
                s.isFreeTrial(),
                s.getCategory(),
                s.getSortOrder(),
                s.getCreatedAt(),
                s.getPaymentMethod(),
                isDollar,
                priceInKrw,
                rate,
                s.getOriginalPrice()
        );
    }
}
