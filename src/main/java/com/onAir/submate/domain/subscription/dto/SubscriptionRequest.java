package com.onAir.submate.domain.subscription.dto;

import com.onAir.submate.domain.subscription.entity.Category;
import com.onAir.submate.domain.subscription.entity.PaymentCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRequest(

        @NotBlank(message = "서비스 이름을 입력해주세요.")
        String serviceName,

        @NotNull(message = "결제 금액을 입력해주세요.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal price,

        @NotNull(message = "결제 주기를 선택해주세요.")
        PaymentCycle paymentCycle,

        @NotNull(message = "다음 결제일을 입력해주세요.")
        LocalDate nextPaymentDate,

        boolean isFreeTrial,

        @NotNull(message = "카테고리를 선택해주세요.")
        Category category
) {}
