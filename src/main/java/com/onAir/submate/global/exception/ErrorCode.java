package com.onAir.submate.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않거나 만료되었습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일로 가입된 계정이 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 정보를 찾을 수 없습니다."),
    SUBSCRIPTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 구독에 대한 권한이 없습니다."),

    // PaymentMethod
    PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "결제수단을 찾을 수 없습니다."),
    PAYMENT_METHOD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 결제수단에 대한 권한이 없습니다."),

    // Report
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 월간 리포트를 찾을 수 없습니다."),

    // Image Parse
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다."),
    IMAGE_PROCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 처리 중 오류가 발생했습니다."),
    IMAGE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "이미지에서 구독 정보를 추출하지 못했습니다."),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
