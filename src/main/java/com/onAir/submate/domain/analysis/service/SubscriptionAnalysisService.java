package com.onAir.submate.domain.analysis.service;

import com.onAir.submate.domain.analysis.dto.OptimizationResponse;
import com.onAir.submate.domain.chat.client.GeminiClient;
import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionAnalysisService {

    private final GeminiClient geminiClient;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public OptimizationResponse analyze(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user);

        String prompt = buildAnalysisPrompt(user, subscriptions);
        String result = geminiClient.generate(prompt);

        List<OptimizationResponse.StatCard> cards = parseCards(result);
        String withoutCards = result.replaceAll("(?s)\\[CARDS\\].*?\\[/CARDS\\]", "").trim();

        String[] parts = withoutCards.split("===DETAIL===", 2);
        String summary = parts[0].trim();
        String detail = parts.length > 1 ? parts[1].trim() : withoutCards;

        return new OptimizationResponse(cards, summary, detail);
    }

    private String buildAnalysisPrompt(User user, List<Subscription> subscriptions) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 개인 구독 비용 최적화 전문가 AI입니다.\n");
        sb.append("사용자의 구독 현황을 분석하고 아래 항목을 한국어 마크다운으로 작성해주세요:\n");
        sb.append("1. **구독 현황 요약** (총 월 지출, 카테고리별 분류)\n");
        sb.append("2. **중복/유사 구독 감지** (같은 카테고리에 여러 서비스가 있는 경우)\n");
        sb.append("3. **비용 절감 추천** (더 저렴한 요금제, 묶음 할인, 해지 추천 등)\n");
        sb.append("4. **사용자 맞춤 추천** (직업과 취미를 고려한 구독 추가/변경 제안)\n\n");

        // 사용자 정보
        sb.append("## 사용자 정보\n");
        sb.append("- 닉네임: ").append(user.getNickname()).append("\n");
        if (user.getJob() != null && !user.getJob().isBlank()) {
            sb.append("- 직업: ").append(user.getJob()).append("\n");
        }
        if (user.getHobby() != null && !user.getHobby().isBlank()) {
            sb.append("- 취미: ").append(user.getHobby()).append("\n");
        }
        sb.append("\n");

        // 구독 목록
        sb.append("## 현재 구독 목록\n");
        if (subscriptions.isEmpty()) {
            sb.append("등록된 구독이 없습니다.\n");
        } else {
            BigDecimal totalMonthly = BigDecimal.ZERO;
            Map<String, List<Subscription>> byCategory = subscriptions.stream()
                    .collect(Collectors.groupingBy(s -> s.getCategory().name()));

            for (Map.Entry<String, List<Subscription>> entry : byCategory.entrySet()) {
                sb.append("\n### ").append(entry.getKey()).append("\n");
                for (Subscription s : entry.getValue()) {
                    BigDecimal monthly = toMonthlyPrice(s);
                    totalMonthly = totalMonthly.add(monthly);
                    sb.append(String.format("- **%s**: %s원/%s (월 환산 약 %s원)%s\n",
                            s.getServiceName(),
                            s.getPrice().toPlainString(),
                            s.getPaymentCycle().name().equals("MONTHLY") ? "월" : "년",
                            monthly.setScale(0, RoundingMode.HALF_UP).toPlainString(),
                            s.isFreeTrial() ? " ⚠️ 무료체험 중" : ""
                    ));
                }
            }
            sb.append(String.format("\n**총 월 지출 (환산): 약 %s원**\n",
                    totalMonthly.setScale(0, RoundingMode.HALF_UP).toPlainString()));
        }

        sb.append("\n위 정보를 바탕으로 구체적이고 실용적인 최적화 분석을 제공해주세요.\n\n");
        sb.append("반드시 아래 형식을 정확히 지켜주세요:\n");
        sb.append("1. 맨 처음에 핵심 수치 3개를 아래 형식으로 작성하세요:\n");
        sb.append("[CARDS]\n");
        sb.append("총 월 지출|₩XX,XXX|wallet\n");
        sb.append("절약 가능 금액|₩X,XXX|trending_down\n");
        sb.append("주의 구독|X건|warning\n");
        sb.append("[/CARDS]\n");
        sb.append("2. 그 아래에 핵심 내용을 bullet point 3줄 이내로 요약하세요.\n");
        sb.append("3. 요약 아래에 정확히 \"===DETAIL===\" 한 줄을 입력하세요.\n");
        sb.append("4. 그 이후에 위 4가지 항목을 상세하게 작성하세요.");

        return sb.toString();
    }

    private List<OptimizationResponse.StatCard> parseCards(String result) {
        List<OptimizationResponse.StatCard> cards = new ArrayList<>();
        int start = result.indexOf("[CARDS]");
        int end = result.indexOf("[/CARDS]");
        if (start == -1 || end == -1) return cards;

        String block = result.substring(start + 7, end).trim();
        for (String line : block.split("\n")) {
            String[] parts = line.trim().split("\\|");
            if (parts.length == 3) {
                cards.add(new OptimizationResponse.StatCard(parts[0].trim(), parts[1].trim(), parts[2].trim()));
            }
        }
        return cards;
    }

    private BigDecimal toMonthlyPrice(Subscription s) {
        if (s.getPaymentCycle().name().equals("YEARLY")) {
            return s.getPrice().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }
        return s.getPrice();
    }
}
