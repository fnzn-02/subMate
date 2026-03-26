package com.onAir.submate.domain.chat.service;

import com.onAir.submate.domain.chat.client.GeminiClient;
import com.onAir.submate.domain.chat.dto.ChatRequest;
import com.onAir.submate.domain.chat.dto.ChatResponse;
import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
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
public class ChatService {

    private final GeminiClient geminiClient;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ChatResponse chat(Long userId, ChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user);

        String prompt = buildPrompt(user.getNickname(), subscriptions, request.message());
        String answer = geminiClient.generate(prompt);

        return new ChatResponse(answer);
    }

    private String buildPrompt(String nickname, List<Subscription> subscriptions, String userMessage) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 구독 서비스 전문 AI 비서입니다. ");
        sb.append("사용자의 현재 구독 현황을 분석하고, 더 저렴한 대체 요금제나 결합 할인을 추천해주세요.\n");
        sb.append("답변은 항상 한국어로, 친절하고 간결하게 작성해주세요.\n\n");

        sb.append("=== 사용자 정보 ===\n");
        sb.append("닉네임: ").append(nickname).append("\n\n");

        sb.append("=== 현재 구독 목록 ===\n");
        if (subscriptions.isEmpty()) {
            sb.append("등록된 구독이 없습니다.\n");
        } else {
            for (Subscription s : subscriptions) {
                sb.append(String.format("- [%s] %s: %s원/%s%s\n",
                        s.getCategory().name(),
                        s.getServiceName(),
                        s.getPrice().toPlainString(),
                        s.getPaymentCycle().name().equals("MONTHLY") ? "월" : "년",
                        s.isFreeTrial() ? " (무료체험 중)" : ""
                ));
            }
        }

        sb.append("\n=== 사용자 질문 ===\n");
        sb.append(userMessage).append("\n");

        return sb.toString();
    }
}
