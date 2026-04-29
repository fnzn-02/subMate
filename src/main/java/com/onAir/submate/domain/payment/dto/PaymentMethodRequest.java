package com.onAir.submate.domain.payment.dto;

import com.onAir.submate.domain.payment.entity.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentMethodRequest(
        @NotNull(message = "결제수단 타입을 선택해주세요.")
        PaymentType type,

        @NotBlank(message = "발급사를 선택해주세요.")
        String issuer,

        String nickname
) {}
