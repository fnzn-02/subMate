# 📦 subMate - 구독 관리 서비스 (Backend)

> 흩어진 구독 서비스를 한 곳에서 관리하고, AI 기반 분석으로 불필요한 지출을 줄여주는 구독 관리 플랫폼

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)

## 📌 프로젝트 소개

구독 서비스가 늘어날수록 매월 어디에 얼마를 쓰는지 파악하기 어려워집니다.
**subMate**는 구독 현황을 한눈에 확인하고, AI 비서를 통해 절약 방법을 제안받을 수 있는 구독 관리 플랫폼입니다.

- 🔗 **Frontend Repository**: [subMate-frontend](https://github.com/fnzn-02/subMate-frontend)

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 회원 인증 | 이메일 인증 기반 회원가입, JWT Stateless 인증 |
| 구독 관리 | 구독 서비스 CRUD, 드래그앤드롭 순서 변경 |
| 대시보드 | 월별 지출 분석, 카테고리별 차트, 결제 캘린더 |
| AI 비서 | Gemini API 기반 구독 데이터 연동 맞춤 분석 |
| 결제 알림 | Spring Batch 기반 D-3, D-1 이메일 알림 발송 |
| 해지 센터 | 주요 구독 서비스 해지 페이지 직접 연결 |

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 4.0, Java 17
- **ORM**: Spring Data JPA, Hibernate
- **Database**: MySQL 8.0
- **Security**: Spring Security, JWT (jjwt 0.12.6)
- **Batch**: Spring Batch (결제 알림 스케줄링)
- **Mail**: Spring Mail (Gmail SMTP)
- **AI**: Google Gemini API (gemini-2.5-flash)
- **Build**: Gradle

### Infra
- **File Storage**: Local FileSystem (프로필 이미지)
- **Auth Flow**: Access Token (1시간) + LocalStorage

## 📁 프로젝트 구조

```
src/main/java/com/onAir/submate/
├── domain/
│   ├── chat/          # AI 챗봇 (Gemini 연동)
│   ├── dashboard/     # 대시보드 통계
│   ├── subscription/  # 구독 관리 (CRUD, 정렬)
│   └── user/          # 회원 인증, 프로필
├── global/
│   ├── batch/         # 결제 알림 스케줄러
│   ├── config/        # WebMvc, RestClient 설정
│   ├── exception/     # 전역 예외 처리
│   ├── infra/         # 파일 스토리지
│   ├── mail/          # 이메일 발송, 인증코드 관리
│   └── security/      # JWT 필터, Security 설정
└── SubmateApplication.java
```

## ⚙️ 로컬 실행 방법

### 사전 요구사항
- Java 17+
- MySQL 8.0+
- Gmail 계정 (앱 비밀번호)
- Google Gemini API Key

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
  expiration: 3600000

gemini:
  api-key: YOUR_GEMINI_API_KEY
  model: gemini-2.5-flash
  base-url: https://generativelanguage.googleapis.com/v1beta/models

file:
  upload-dir: ${user.home}/submate-uploads/profiles
  profile-url-prefix: /uploads/profiles

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/submate
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD
  mail:
    host: smtp.gmail.com
    port: 587
    username: YOUR_GMAIL
    password: YOUR_GMAIL_APP_PASSWORD
```

### 3. 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 📡 주요 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/email/send-code` | 이메일 인증코드 발송 |
| GET | `/api/subscriptions` | 구독 목록 조회 |
| POST | `/api/subscriptions` | 구독 추가 |
| GET | `/api/dashboard` | 대시보드 통계 |
| POST | `/api/chat` | AI 비서 대화 |
| POST | `/api/users/me/profile-image` | 프로필 이미지 업로드 |

## 🔐 보안

- `application-local.yml` — `.gitignore` 처리 (DB 비밀번호, API 키 등 민감 정보 제외)
- JWT Stateless 인증으로 서버 세션 미사용
- Spring Security 기반 엔드포인트별 권한 제어
