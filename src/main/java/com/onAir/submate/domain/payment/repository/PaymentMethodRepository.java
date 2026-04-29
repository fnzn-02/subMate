package com.onAir.submate.domain.payment.repository;

import com.onAir.submate.domain.payment.entity.PaymentMethod;
import com.onAir.submate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByUserOrderByCreatedAtDesc(User user);
}
