package com.onAir.submate.domain.payment.entity;

import com.onAir.submate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_methods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentType type;

    @Column(nullable = false, length = 50)
    private String issuer;

    @Column(nullable = false, length = 50)
    private String nickname;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PaymentMethod(User user, PaymentType type, String issuer, String nickname) {
        this.user = user;
        this.type = type;
        this.issuer = issuer;
        this.nickname = nickname;
    }

    public String getDisplayName() {
        return nickname.isBlank() ? issuer : issuer + " (" + nickname + ")";
    }
}
