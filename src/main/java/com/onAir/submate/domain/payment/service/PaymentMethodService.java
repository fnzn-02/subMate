package com.onAir.submate.domain.payment.service;

import com.onAir.submate.domain.payment.dto.PaymentMethodRequest;
import com.onAir.submate.domain.payment.dto.PaymentMethodResponse;
import com.onAir.submate.domain.payment.entity.PaymentMethod;
import com.onAir.submate.domain.payment.repository.PaymentMethodRepository;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getMyPaymentMethods(Long userId) {
        User user = getUser(userId);
        return paymentMethodRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional
    public PaymentMethodResponse create(Long userId, PaymentMethodRequest request) {
        User user = getUser(userId);
        PaymentMethod pm = PaymentMethod.builder()
                .user(user)
                .type(request.type())
                .issuer(request.issuer())
                .nickname(request.nickname() != null ? request.nickname() : "")
                .build();
        return PaymentMethodResponse.from(paymentMethodRepository.save(pm));
    }

    @Transactional
    public void delete(Long userId, Long paymentMethodId) {
        PaymentMethod pm = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
        if (!pm.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.PAYMENT_METHOD_ACCESS_DENIED);
        }
        paymentMethodRepository.delete(pm);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
