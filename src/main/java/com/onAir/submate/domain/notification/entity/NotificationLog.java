package com.onAir.submate.domain.notification.entity;

import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "type", "sent_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private NotificationType type;

    @Column(name = "sent_date", nullable = false)
    private java.time.LocalDate sentDate;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public NotificationLog(User user, Subscription subscription,
                           NotificationType type, java.time.LocalDate sentDate) {
        this.user = user;
        this.subscription = subscription;
        this.type = type;
        this.sentDate = sentDate;
        this.sentAt = LocalDateTime.now();
    }
}
