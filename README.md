# SubMate — AI 기반 구독 관리 플랫폼 (Backend)

> 흩어진 구독을 한 곳에서, AI가 똑똑하게 관리합니다.

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20RDS%20%7C%20S3-FF9900?style=flat-square&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)
[![Gemini](https://img.shields.io/badge/Gemini-2.5_Flash-4285F4?style=flat-square&logo=google&logoColor=white)](https://deepmind.google/technologies/gemini/)

**연성대학교 캡스톤디자인 2026 — Team Onair**

---

## 프로젝트 소개

한국인은 평균 **5.8개**의 구독 서비스를 사용하지만, 38%는 다음 결제일을 모르고 23%는 거의 쓰지 않는 구독을 유지합니다. SubMate는 이 문제를 해결하기 위해 만들어졌습니다.

- 영수증·결제 알림 사진 한 장으로 **자동 구독 등록** (OCR)
- **AI 어시스턴트**가 내 데이터를 기반으로 절약 인사이트 제공
- **OTT 통합 검색**으로 내 구독에서 볼 수 있는 콘텐츠 즉시 확인
- **능동적 알림**으로 결제일·무료체험 종료를 미리 알림

Flutter 앱 레포지토리: [submate_app](https://github.com/fnzn-02/submate_app)

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| 회원 인증 | 이메일 인증 기반 회원가입, JWT Stateless 인증 |
| 구독 CRUD | 구독 서비스 등록·수정·삭제, 드래그앤드롭 순서 변경 |
| OCR 자동 등록 | 영수증·청구서 이미지에서 서비스명·금액·결제일 자동 추출 |
| 대시보드 | 월별 지출 합계, 카테고리별 파이차트, 결제 캘린더, D-Day 카운트다운 |
| AI 어시스턴트 | Gemini 기반 구독 데이터 연동 맞춤 분석 및 절약 추천 |
| AI 분석 리포트 | 매월 자동 생성되는 구독 분석 리포트 (요약·상세·월간) |
| OTT 통합 검색 | TMDB 연동 4만+ 콘텐츠, 내 구독 OTT 매칭 표시 |
| 환율 자동 환산 | USD 구독을 실시간 환율 기준 원화로 자동 변환 |
| 결제 알림 | FCM 기반 결제 D-7·D-3·D-0 푸시, 무료체험 만료 알림 |
| 이메일 알림 | Spring Batch 기반 결제 임박 이메일 발송 |
| 마이페이지 | 프로필 이미지, 닉네임·비밀번호 변경, 테마 설정, 회원 탈퇴 |

---

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.x, Java 17
- **ORM**: Spring Data JPA, Hibernate
- **Security**: Spring Security, JWT (jjwt 0.12.6)
- **Batch**: Spring Batch (결제 알림 스케줄링)
- **Mail**: Brevo SMTP API
- **AI**: Google Gemini 2.5 Flash API
- **Build**: Gradle

### Infrastructure (AWS)
- **Server**: EC2 (Ubuntu 24.04)
- **Database**: RDS MySQL 8.0
- **Storage**: S3 (파일 저장)
- **Serverless**: Lambda

---

## 프로젝트 구조

```
src/main/java/com/onAir/submate/
├── domain/
│   ├── analysis/      # AI 분석 리포트
│   ├── chat/          # AI 챗봇 (Gemini)
│   ├── dashboard/     # 대시보드 통계
│   ├── exchange/      # 실시간 환율
│   ├── notification/  # FCM 푸시 알림
│   ├── ott/           # OTT 통합 검색 (TMDB)
│   ├── payment/       # 결제 관리
│   ├── report/        # 월간 리포트
│   ├── subscription/  # 구독 CRUD
│   └── user/          # 회원 인증·프로필
└── global/
    ├── batch/         # 이메일 알림 스케줄러
    ├── config/        # Firebase, CORS, RestClient 설정
    ├── exception/     # 전역 예외 처리
    ├── mail/          # 이메일 발송
    └── security/      # JWT 필터, Security 설정
```

---

## 로컬 실행 방법

### 사전 요구사항
- Java 17+
- MySQL 8.0+
- Google Gemini API Key
- Brevo API Key (이메일 발송)
- Firebase 프로젝트 (FCM 푸시)

### 1. 저장소 클론

```bash
git clone https://github.com/fnzn-02/subMate.git
cd subMate
```

### 2. 환경 설정

`src/main/resources/application-local.yml` 파일 생성 후 아래 내용 입력:

```yaml
jwt:
  secret: YOUR_JWT_SECRET
  expiration: 604800000

gemini:
  api-key: YOUR_GEMINI_API_KEY
  model: gemini-2.5-flash
  base-url: https://generativelanguage.googleapis.com/v1beta/models

brevo:
  api-key: YOUR_BREVO_API_KEY
  sender-email: YOUR_EMAIL

koreaexim:
  api-key: YOUR_KOREAEXIM_API_KEY

tmdb:
  api-key: YOUR_TMDB_API_KEY
  base-url: https://api.themoviedb.org/3
  image-base-url: https://image.tmdb.org/t/p/w200

file:
  upload-dir: ${user.home}/submate-uploads/profiles
  profile-url-prefix: /uploads/profiles

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/submate
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD
  jpa:
    hibernate:
      ddl-auto: update
  batch:
    job:
      enabled: false
    jdbc:
      initialize-schema: always
```

`src/main/resources/firebase-service-account.json` — Firebase 콘솔에서 다운로드한 서비스 계정 키 파일 배치

### 3. 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 주요 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/email/send-code` | 이메일 인증코드 발송 |
| GET | `/api/subscriptions` | 구독 목록 조회 |
| POST | `/api/subscriptions` | 구독 추가 |
| POST | `/api/subscriptions/ocr` | OCR 구독 자동 등록 |
| GET | `/api/dashboard` | 대시보드 통계 |
| POST | `/api/chat` | AI 어시스턴트 대화 |
| GET | `/api/analysis` | AI 분석 리포트 |
| GET | `/api/ott/search` | OTT 콘텐츠 검색 |
| GET | `/api/exchange` | 실시간 환율 조회 |

---

## 팀 소개

**Team Onair — 연성대학교 캡스톤디자인 2026**

| 이름 | 역할 |
|------|------|
| 전대향 | 팀장 · Backend |
| 김대호 | Database |
| 김한성 | Frontend (Flutter) |
| 전재우 | Research · 발표 |
| 김건 | Design · PPT |

---

## 보안 안내

- `application-local.yml`, `application-prod.yml`, `firebase-service-account.json`, `deploy.sh` — `.gitignore` 처리
- JWT Stateless 인증으로 서버 세션 미사용
- Spring Security 기반 엔드포인트별 권한 제어
