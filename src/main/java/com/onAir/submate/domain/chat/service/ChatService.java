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

        sb.append("당신은 구독 서비스 전문 AI 비서입니다.\n");
        sb.append("규칙:\n");
        sb.append("1. 매 답변 앞에 인사말(안녕하세요 등)을 붙이지 마세요. 바로 본론부터 시작하세요.\n");
        sb.append("2. 인사(안녕, ㅎㅇ 등)에는 친근하게 한 문장으로 응답하세요.\n");
        sb.append("3. '뭐 물어볼 수 있어?' 같은 질문엔 구독 관련 질문 예시 3~4가지를 알려주세요.\n");
        sb.append("4. 구독 분석, 추천, 절약 방법 등 구독 관련 질문에는 아래 구독 목록을 활용해 구체적으로 답하세요.\n");
        sb.append("5. 구독과 무관한 일반 질문에도 성실하게 답하세요.\n");
        sb.append("6. 답변은 한국어로, 마크다운 기호(**, ##, - 등) 없이 일반 텍스트로 작성하세요.\n");
        sb.append("7. 불필요하게 길지 않게, 핵심만 간결하게 답하세요.\n\n");

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
