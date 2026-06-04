package com.onAir.submate.domain.subscription.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onAir.submate.domain.chat.client.GeminiClient;
import com.onAir.submate.domain.subscription.dto.ParsedSubscriptionDto;
import com.onAir.submate.domain.subscription.entity.Category;
import com.onAir.submate.domain.subscription.entity.PaymentCycle;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageParseService {

    private final GeminiClient geminiClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> BILLING_NAME_MAP = Map.ofEntries(
        Map.entry("anthropic", "Claude"),
        Map.entry("openai", "ChatGPT"),
        Map.entry("spotify ab", "스포티파이"),
        Map.entry("spotify korea", "스포티파이"),
        Map.entry("netflix", "넷플릭스"),
        Map.entry("disney plus", "디즈니+"),
        Map.entry("disney+", "디즈니+"),
        Map.entry("tving", "티빙"),
        Map.entry("watcha", "왓챠"),
        Map.entry("wavve", "웨이브"),
        Map.entry("melon", "멜론"),
        Map.entry("bugs", "벅스"),
        Map.entry("genie", "지니뮤직"),
        Map.entry("coupang", "쿠팡 로켓와우"),
        Map.entry("woowa brothers", "배달의민족"),
        Map.entry("baemin", "배달의민족"),
        Map.entry("notion labs", "노션"),
        Map.entry("notion", "노션"),
        Map.entry("duolingo", "듀오링고"),
        Map.entry("ridi", "리디북스"),
        Map.entry("millie", "밀리의서재"),
        Map.entry("microsoft", "OneDrive"),
        Map.entry("naver", "네이버플러스 멤버십")
    );

    private static final java.util.List<String> KNOWN_SERVICES = java.util.List.of(
        "Claude", "ChatGPT", "Gemini", "넷플릭스", "스포티파이", "디즈니+",
        "티빙", "왓챠", "웨이브", "유튜브 프리미엄", "Apple Music",
        "멜론", "벅스", "FLO", "지니뮤직", "노션", "듀오링고",
        "쿠팡 로켓와우", "배달의민족", "구글 드라이브", "OneDrive",
        "밀리의서재", "리디북스", "네이버플러스 멤버십"
    );

    private String normalizeServiceName(String name) {
        if (name == null) return null;
        // 1. 청구명 → 서비스명 매핑 테이블
        String mapped = BILLING_NAME_MAP.get(name.toLowerCase().trim());
        if (mapped != null) return mapped;
        // 2. "Claude Pro", "ChatGPT Plus" 등 플랜명 제거
        for (String service : KNOWN_SERVICES) {
            if (name.toLowerCase().startsWith(service.toLowerCase())) {
                return service;
            }
        }
        return name;
    }

    private static final String PARSE_PROMPT = """
            이 이미지는 구독 서비스 결제 관련 화면(영수증, 이메일, 앱 스크린샷 등)입니다.
            이미지에서 다음 정보를 추출하여 반드시 아래 JSON 형식으로만 응답하세요. 설명 없이 JSON만 출력하세요.

            {
              "serviceName": "서비스명 (예: Netflix, Spotify, 유튜브 프리미엄)",
              "price": 숫자만 (소수점 포함 가능, 예: 17000 or 13.99),
              "currency": "KRW 또는 USD",
              "paymentCycle": "MONTHLY 또는 YEARLY",
              "nextPaymentDate": "YYYY-MM-DD 형식 (없으면 null)",
              "category": "OTT, MUSIC, AI, GAME, CLOUD, EDUCATION, SHOPPING, DELIVERY, OTHER 중 하나"
            }

            - 정보를 확인할 수 없는 필드는 null로 설정하세요.
            - 날짜가 이미지에 없으면 nextPaymentDate를 null로 설정하세요.
            - 반드시 JSON만 출력하고 마크다운 코드블록(```)을 사용하지 마세요.
            - serviceName은 청구사명이 아닌 실제 서비스 제품명으로 반환하세요. 예: Anthropic → Claude, OpenAI → ChatGPT, Spotify AB → 스포티파이
            - serviceName은 플랜·등급명을 제외한 기본 서비스명만 반환하세요. 예: Claude Pro → Claude, ChatGPT Plus → ChatGPT, Gemini Advanced → Gemini, YouTube Premium → 유튜브 프리미엄
            """;

    public ParsedSubscriptionDto parseImage(MultipartFile file) {
        String mimeType = file.getContentType();
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (IOException e) {
            log.error("이미지 파일 읽기 실패", e);
            throw new CustomException(ErrorCode.IMAGE_PROCESS_FAILED);
        }

        String rawResponse = geminiClient.generateWithImage(PARSE_PROMPT, imageBytes, mimeType);
        log.info("Gemini 이미지 파싱 응답: {}", rawResponse);

        return parseGeminiResponse(rawResponse);
    }

    private ParsedSubscriptionDto parseGeminiResponse(String rawResponse) {
        String json = extractJson(rawResponse);
        try {
            JsonNode root = objectMapper.readTree(json);

            String serviceName = normalizeServiceName(getTextOrNull(root, "serviceName"));
            BigDecimal price = getPriceOrNull(root, "price");
            String currency = getTextOrNull(root, "currency");
            PaymentCycle paymentCycle = getPaymentCycleOrDefault(root, "paymentCycle");
            LocalDate nextPaymentDate = getDateOrNull(root, "nextPaymentDate");
            Category category = getCategoryOrDefault(root, "category");

            // 결제일이 오늘 이하면 다음 주기로 이동
            if (nextPaymentDate != null && !nextPaymentDate.isAfter(LocalDate.now())) {
                nextPaymentDate = paymentCycle == PaymentCycle.YEARLY
                        ? nextPaymentDate.plusYears(1)
                        : nextPaymentDate.plusMonths(1);
            }

            return new ParsedSubscriptionDto(
                    serviceName,
                    price,
                    currency != null ? currency : "KRW",
                    paymentCycle,
                    nextPaymentDate,
                    category
            );
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패. 응답: {}", rawResponse, e);
            throw new CustomException(ErrorCode.IMAGE_PARSE_FAILED);
        }
    }

    private String extractJson(String response) {
        if (response == null || response.isBlank()) {
            throw new CustomException(ErrorCode.IMAGE_PARSE_FAILED);
        }
        // 마크다운 코드블록 제거
        Pattern codeBlock = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        Matcher matcher = codeBlock.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // JSON 객체 직접 추출
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }
        return response.trim();
    }

    private String getTextOrNull(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || node.asText().isBlank()) return null;
        return node.asText().trim();
    }

    private BigDecimal getPriceOrNull(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) return null;
        try {
            return new BigDecimal(node.asText().replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private PaymentCycle getPaymentCycleOrDefault(JsonNode root, String field) {
        try {
            String val = getTextOrNull(root, field);
            if (val == null) return PaymentCycle.MONTHLY;
            return PaymentCycle.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PaymentCycle.MONTHLY;
        }
    }

    private LocalDate getDateOrNull(JsonNode root, String field) {
        String val = getTextOrNull(root, field);
        if (val == null) return null;
        try {
            return LocalDate.parse(val, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {}", val);
            return null;
        }
    }

    private Category getCategoryOrDefault(JsonNode root, String field) {
        try {
            String val = getTextOrNull(root, field);
            if (val == null) return Category.OTHER;
            return Category.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Category.OTHER;
        }
    }
}
