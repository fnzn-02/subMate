package com.onAir.submate.domain.report.entity;

import com.onAir.submate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_reports",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "report_month"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_month", nullable = false, length = 7)
    private String yearMonth; // 예: "2026-04"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // AI 생성 리포트 텍스트

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "saved_amount", precision = 12, scale = 2)
    private BigDecimal savedAmount; // 절약 가능 금액

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MonthlyReport(User user, String yearMonth, String content,
                         BigDecimal totalAmount, BigDecimal savedAmount) {
        this.user = user;
        this.yearMonth = yearMonth;
        this.content = content;
        this.totalAmount = totalAmount;
        this.savedAmount = savedAmount;
    }
}
