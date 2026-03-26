package com.onAir.submate.domain.subscription.entity;

import com.onAir.submate.domain.subscription.entity.Category;
import com.onAir.submate.domain.subscription.entity.PaymentCycle;
import com.onAir.submate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_cycle", nullable = false, length = 10)
    private PaymentCycle paymentCycle;

    @Column(name = "next_payment_date", nullable = false)
    private LocalDate nextPaymentDate;

    @Column(name = "is_free_trial", nullable = false)
    private boolean isFreeTrial = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Subscription(User user, String serviceName, BigDecimal price,
                        PaymentCycle paymentCycle, LocalDate nextPaymentDate,
                        boolean isFreeTrial, Category category) {
        this.user = user;
        this.serviceName = serviceName;
        this.price = price;
        this.paymentCycle = paymentCycle;
        this.nextPaymentDate = nextPaymentDate;
        this.isFreeTrial = isFreeTrial;
        this.category = category;
    }

    public void update(String serviceName, BigDecimal price, PaymentCycle paymentCycle,
                       LocalDate nextPaymentDate, boolean isFreeTrial, Category category) {
        this.serviceName = serviceName;
        this.price = price;
        this.paymentCycle = paymentCycle;
        this.nextPaymentDate = nextPaymentDate;
        this.isFreeTrial = isFreeTrial;
        this.category = category;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
