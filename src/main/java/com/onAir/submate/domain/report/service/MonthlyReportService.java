package com.onAir.submate.domain.report.service;

import com.onAir.submate.domain.chat.client.GeminiClient;
import com.onAir.submate.domain.report.dto.MonthlyReportResponse;
import com.onAir.submate.domain.report.entity.MonthlyReport;
import com.onAir.submate.domain.report.repository.MonthlyReportRepository;
import com.onAir.submate.domain.subscription.entity.PaymentCycle;
import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import com.onAir.submate.global.infra.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final MonthlyReportRepository monthlyReportRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final FcmService fcmService;

    /**
     * 특정 사용자의 이전 달 리포트를 생성하고 저장합니다.
     * 중복 생성 방지: 이미 존재하면 건너뜁니다.
     */
    @Transactional
    public void generateReportForUser(User user, String yearMonth) {
        if (monthlyReportRepository.findByUserIdAndYearMonth(user.getId(), yearMonth).isPresent()) {
            log.info("월간 리포트 이미 존재 - userId={}, yearMonth={}", user.getId(), yearMonth);
            return;
        }

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user);

        if (subscriptions.isEmpty()) {
            log.info("구독 없음 - 리포트 생성 건너뜀 userId={}", user.getId());
            return;
        }

        BigDecimal totalMonthly = calcTotalMonthly(subscriptions);
        String prompt = buildReportPrompt(user, subscriptions, totalMonthly, yearMonth);

        String content;
        try {
            content = geminiClient.generate(prompt, 2048);
        } catch (Exception e) {
            log.error("Gemini 리포트 생성 실패 - userId={}", user.getId(), e);
            content = buildFallbackReport(subscriptions, totalMonthly, yearMonth);
        }

        MonthlyReport report = MonthlyReport.builder()
                .user(user)
                .yearMonth(yearMonth)
                .content(content)
                .totalAmount(totalMonthly)
                .savedAmount(BigDecimal.ZERO)
                .build();

        monthlyReportRepository.save(report);

        // FCM 푸시 알림
        if (user.getFcmToken() != null) {
            fcmService.sendNotification(
                    user.getFcmToken(),
                    yearMonth + " 월간 리포트가 도착했어요!",
                    String.format("이번 달 구독 총 지출: %s원. 지금 확인해보세요.",
                            totalMonthly.setScale(0, RoundingMode.HALF_UP).toPlainString())
            );
        }

        log.info("월간 리포트 생성 완료 - userId={}, yearMonth={}", user.getId(), yearMonth);
    }

    /**
     * 모든 사용자의 이전 달 리포트를 일괄 생성합니다. (스케줄러 호출용)
     */
    @Transactional
    public void generateAllReports() {
        String lastMonth = YearMonth.now().minusMonths(1).toString(); // "2026-03"
        List<User> allUsers = userRepository.findAll();
        log.info("월간 리포트 일괄 생성 시작 - 대상 {} 명, yearMonth={}", allUsers.size(), lastMonth);

        for (User user : allUsers) {
            try {
                generateReportForUser(user, lastMonth);
            } catch (Exception e) {
                log.error("월간 리포트 생성 실패 - userId={}", user.getId(), e);
            }
        }
    }

    /**
     * 특정 월 리포트 재생성 (기존 삭제 후 새로 생성)
     */
    @Transactional
    public MonthlyReportResponse regenerateReport(Long userId, String yearMonth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        monthlyReportRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .ifPresent(monthlyReportRepository::delete);
        monthlyReportRepository.flush();

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user);

        if (subscriptions.isEmpty()) throw new CustomException(ErrorCode.REPORT_NOT_FOUND);

        BigDecimal totalMonthly = calcTotalMonthly(subscriptions);
        String prompt = buildReportPrompt(user, subscriptions, totalMonthly, yearMonth);

        String content;
        try {
            content = geminiClient.generate(prompt, 2048);
        } catch (Exception e) {
            log.error("Gemini 리포트 재생성 실패 - userId={}", userId, e);
            content = buildFallbackReport(subscriptions, totalMonthly, yearMonth);
        }

        MonthlyReport report = MonthlyReport.builder()
                .user(user).yearMonth(yearMonth).content(content)
                .totalAmount(totalMonthly).savedAmount(BigDecimal.ZERO).build();

        return MonthlyReportResponse.from(monthlyReportRepository.save(report));
    }

    /**
     * 현재 달 리포트 수동 생성 (이미 있으면 기존 반환)
     */
    @Transactional
    public MonthlyReportResponse generateCurrentMonth(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String yearMonth = YearMonth.now().toString();

        Optional<MonthlyReport> existing = monthlyReportRepository.findByUserIdAndYearMonth(userId, yearMonth);
        if (existing.isPresent()) {
            return MonthlyReportResponse.from(existing.get());
        }

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user);

        if (subscriptions.isEmpty()) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }

        BigDecimal totalMonthly = calcTotalMonthly(subscriptions);
        String prompt = buildReportPrompt(user, subscriptions, totalMonthly, yearMonth);

        String content;
        try {
            content = geminiClient.generate(prompt, 2048);
        } catch (Exception e) {
            log.error("Gemini 리포트 생성 실패 - userId={}", userId, e);
            content = buildFallbackReport(subscriptions, totalMonthly, yearMonth);
        }

        MonthlyReport report = MonthlyReport.builder()
                .user(user)
                .yearMonth(yearMonth)
                .content(content)
                .totalAmount(totalMonthly)
                .savedAmount(BigDecimal.ZERO)
                .build();

        monthlyReportRepository.save(report);
        return MonthlyReportResponse.from(report);
    }

    /**
     * 내 리포트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MonthlyReportResponse> getMyReports(Long userId) {
        return monthlyReportRepository.findByUserIdOrderByYearMonthDesc(userId)
                .stream()
                .map(MonthlyReportResponse::from)
                .toList();
    }

    /**
     * 특정 월 리포트 조회
     */
    @Transactional(readOnly = true)
    public MonthlyReportResponse getReport(Long userId, String yearMonth) {
        MonthlyReport report = monthlyReportRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        return MonthlyReportResponse.from(report);
    }

    private BigDecimal calcTotalMonthly(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(s -> s.getPaymentCycle() == PaymentCycle.YEARLY
                        ? s.getPrice().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                        : s.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String buildReportPrompt(User user, List<Subscription> subscriptions,
                                     BigDecimal totalMonthly, String yearMonth) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 개인 재무 AI 비서입니다. 사용자의 ").append(yearMonth).append(" 월간 구독 리포트를 한국어 마크다운으로 작성해주세요.\n\n");

        sb.append("## 사용자 정보\n");
        sb.append("- 닉네임: ").append(user.getNickname()).append("\n");
        if (user.getJob() != null && !user.getJob().isBlank()) {
            sb.append("- 직업: ").append(user.getJob()).append("\n");
        }
        if (user.getHobby() != null && !user.getHobby().isBlank()) {
            sb.append("- 취미: ").append(user.getHobby()).append("\n");
        }
        sb.append("\n");

        sb.append("## 이번 달 구독 현황\n");
        Map<String, List<Subscription>> byCategory = subscriptions.stream()
                .collect(Collectors.groupingBy(s -> s.getCategory().name()));

        for (Map.Entry<String, List<Subscription>> entry : byCategory.entrySet()) {
            sb.append("\n### ").append(entry.getKey()).append("\n");
            for (Subscription s : entry.getValue()) {
                BigDecimal monthly = s.getPaymentCycle() == PaymentCycle.YEARLY
                        ? s.getPrice().divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP)
                        : s.getPrice();
                sb.append(String.format("- **%s**: %s원/%s (월 환산 %s원)%s\n",
                        s.getServiceName(),
                        s.getPrice().setScale(0, RoundingMode.HALF_UP).toPlainString(),
                        s.getPaymentCycle() == PaymentCycle.MONTHLY ? "월" : "년",
                        monthly.toPlainString(),
                        s.isFreeTrial() ? " ⚠️ 무료체험 중 (곧 유료 전환 확인 필요)" : ""
                ));
            }
        }

        sb.append(String.format("\n**총 월 지출: 약 %s원**\n\n",
                totalMonthly.setScale(0, RoundingMode.HALF_UP).toPlainString()));

        sb.append("위 내용을 바탕으로 월간 리포트를 작성해주세요.\n\n");
        sb.append("반드시 아래 형식을 정확히 지켜주세요:\n");
        sb.append("1. 맨 처음에 핵심 수치 3개를 아래 형식으로 작성하세요:\n");
        sb.append("[CARDS]\n");
        sb.append("총 월 지출|₩XX,XXX|wallet\n");
        sb.append("가장 많은 지출|카테고리명|category\n");
        sb.append("절약 팁|X가지|lightbulb\n");
        sb.append("[/CARDS]\n");
        sb.append("2. 그 아래에 핵심 내용을 bullet point 3줄 이내로 요약하세요.\n");
        sb.append("3. 요약 아래에 정확히 \"===DETAIL===\" 한 줄을 입력하세요.\n");
        sb.append("4. 그 이후에 아래 4가지를 상세하게 작성하세요:\n");
        sb.append("   - **이달의 구독 요약** - 총 지출, 카테고리별 비중\n");
        sb.append("   - **주목할 점** - 무료체험 만료 임박, 비용이 큰 구독 등\n");
        sb.append("   - **절약 팁** - 구체적인 절약 방법 2~3가지\n");
        sb.append("   - **다음 달 예상** - 결제 예정 금액 안내\n");
        sb.append("친근하고 격려하는 톤으로 작성해주세요.");

        return sb.toString();
    }

    private String buildFallbackReport(List<Subscription> subscriptions, BigDecimal totalMonthly, String yearMonth) {
        return String.format("""
                ## %s 월간 구독 리포트

                이번 달 총 구독 지출은 약 **%s원**입니다.
                총 %d개의 구독 서비스를 이용 중입니다.

                자세한 AI 분석은 잠시 후 다시 시도해주세요.
                """,
                yearMonth,
                totalMonthly.setScale(0, RoundingMode.HALF_UP).toPlainString(),
                subscriptions.size()
        );
    }
}
