package com.onAir.submate.domain.payment.dto;

import com.onAir.submate.domain.payment.entity.PaymentMethod;
import com.onAir.submate.domain.payment.entity.PaymentType;

public record PaymentMethodResponse(
        Long id,
        PaymentType type,
        String issuer,
        String nickname,
        String displayName
) {
    public static PaymentMethodResponse from(PaymentMethod p) {
        return new PaymentMethodResponse(
                p.getId(),
                p.getType(),
                p.getIssuer(),
                p.getNickname(),
                p.getDisplayName()
        );
    }
}
