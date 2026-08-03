# HPet Backend — Phase 0 + Phase 1 + Phase 2 (전체 완료)

`./gradlew bootRun` 한 번으로 뜨고, Swagger에서 회원가입 → 이메일 인증 → 약관 동의 →
로그인 → 건강 프로필 저장 → AI 추천 확인 → 영양제 등록(캐릭터 자동 배정) → 캐릭터 조회 →
코스튬 착용까지 전체 흐름을 눈으로 확인할 수 있습니다.

## 포함된 것

### Phase 0. 공통 인프라 — ✅ 완료
- 공통 응답 포맷 `ApiResponse<T>`, 전역 예외 처리, requestId 로깅 필터, Swagger

### Phase 1. 인증·온보딩 — ✅ 완료 (소셜 로그인 제외, 전체 항목 구현)
| 기능 | API |
|---|---|
| 회원가입 (가입 즉시 인증코드 자동 발송) | `POST /api/auth/signup` |
| 이메일 인증코드 재발송 | `POST /api/auth/email-verification/send` |
| 이메일 인증코드 확인 | `POST /api/auth/email-verification/confirm` |
| 로그인 (JWT 발급) | `POST /api/auth/login` |
| 토큰 재발급 | `POST /api/auth/reissue` |
| 로그아웃 | `POST /api/auth/logout` |
| 비밀번호 재설정 요청 | `POST /api/auth/password-reset/request` |
| 비밀번호 재설정 확인 | `POST /api/auth/password-reset/confirm` |
| **약관/건강정보 동의 제출 (이력 저장)** | `POST /api/users/me/agreements` |
| **내 동의 이력 조회** | `GET /api/users/me/agreements` |
| 내 정보 조회 (인증 필요) | `GET /api/users/me` |

**약관 동의 관련 참고**: `TERMS_OF_SERVICE`(이용약관), `PRIVACY_POLICY`(개인정보처리방침),
`HEALTH_INFO_COLLECTION`(건강정보 수집 동의) 3가지가 필수 항목입니다. 셋 중 하나라도
`agreed:true`로 제출되지 않으면 400 에러가 납니다. 동의/비동의 여부와 관계없이 매 제출마다
새 이력으로 쌓이므로(현재 상태가 아니라 히스토리), "언제 어떤 버전에 동의했는지" 추적이 가능합니다.

**⚠️ 이메일 인증코드 / 비밀번호 재설정 토큰은 실제 메일로 발송되지 않습니다.**
`ConsoleEmailSender`가 서버 콘솔(IntelliJ 하단 실행 로그)에 아래처럼 찍어줍니다:
```
=== [MOCK EMAIL] ===================================
To      : test@hpet.com
Subject : [HPet] 이메일 인증코드
Body    : 인증코드: 482913 (유효시간 5분)
=====================================================
```
이 값을 그대로 Swagger의 confirm API에 입력하면 됩니다. 나중에 실제 메일 서버가 준비되면
`common/email/EmailSender` 인터페이스를 구현하는 클래스만 새로 만들면 되고, 나머지 로직(코드
생성/만료/검증)은 그대로 재사용됩니다.

### Phase 2. 건강 프로필 · 캐릭터 배정 — ✅ 완료 (전체 항목 구현)
| 기능 | API |
|---|---|
| 건강 프로필 저장 (upsert) | `POST /api/profile` |
| 내 건강 프로필 조회 | `GET /api/profile/me` |
| **AI 맞춤 영양제 추천 (규칙 기반)** | `GET /api/profile/recommendations` |
| 영양제 검색 (마스터데이터) | `GET /api/supplements?keyword=` |
| 내 영양제 등록 (**처음 등록 시 캐릭터 자동 배정**) | `POST /api/users/me/supplements` |
| 내 영양제 목록 조회 | `GET /api/users/me/supplements` |
| 내 캐릭터 조회 (단계/착용아이템 포함) | `GET /api/character/me` |
| 코스튬 착용 (7일차부터) | `POST /api/character/me/items` |
| 코스튬 해제 | `DELETE /api/character/me/items/{itemId}` |

**AI 추천 로직 참고**: "AI"라고 부르지만 MVP는 규칙 기반입니다 (문서 2-4 확정 방향).
건강 프로필의 나이(40세 이상 → 칼슘/오메가3), 성별(여성 → 철분), 메모 키워드
(피로/수면 → 마그네슘·멜라토닌, 탈모/모발 → 비오틴, 소화 → 유산균, 자세 → 칼슘)에 따라
추천 결과가 달라집니다. 아무 규칙에도 안 걸리면 기본으로 비타민을 추천합니다.
호출 전에 `POST /api/profile`로 건강 프로필을 먼저 저장해야 합니다.

앱이 처음 뜰 때 `DataSeeder`가 영양제 8종(철분/칼슘/오메가3/비타민/마그네슘/멜라토닌/비오틴/유산균)과
캐릭터 4종(거북이/병아리/수달/고슴도치)을 자동으로 시드합니다.

**캐릭터 배정 로직** (회의 1-2 확정): 철분·칼슘·오메가3 → 거북이 / 비타민 → 병아리 /
마그네슘·멜라토닌 → 수달 / 비오틴 → 고슴도치. 유산균은 매핑 대상 아님.
여러 캐릭터에 매칭되는 영양제를 함께 등록하면 후보 중 랜덤으로 배정됩니다.

**성장 단계**: 지금은 `growthDays = 0`으로 시작해서 항상 "아기" 단계로 보입니다.
실제로 날짜가 쌓이는 로직(포션 지급)은 Phase 5에서 구현 예정이라, 코스튬 착용 API를
테스트하려면 `growthDays`를 DB(H2 콘솔)에서 7 이상으로 직접 바꿔보시면 됩니다.

## 아직 안 만든 것
- Phase 3 (홈 대시보드), Phase 4 (복용기록·자세교정), Phase 5 (알림·AI비전인증·포션), Phase 6 (통합테스트)

Phase 1, 2는 문서 체크리스트 기준 전 항목 구현 완료입니다 (소셜 로그인만 스킵 확정).

## 실행 방법

Gradle Wrapper 바이너리는 포함돼 있지 않습니다. IntelliJ에서 "Open as Gradle Project"로
열면 IDE가 알아서 처리해줍니다 (자세한 건 이전 안내 참고).

## curl로 전체 흐름 데모하기

```bash
# 1) 회원가입 (콘솔 로그에 인증코드가 찍힘)
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@hpet.com","password":"password123"}'

# 2) 콘솔 로그에서 본 인증코드로 이메일 인증 확인
curl -X POST http://localhost:8080/api/auth/email-verification/confirm \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@hpet.com","code":"콘솔에서_본_코드"}'

# 3) 로그인 → accessToken 받기
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@hpet.com","password":"password123"}'

TOKEN="위에서 받은 accessToken"

# 3-1) 약관 동의 제출 (3개 필수 항목 모두 agreed:true)
curl -X POST http://localhost:8080/api/users/me/agreements \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '[
    {"type":"TERMS_OF_SERVICE","version":"v1.0","agreed":true},
    {"type":"PRIVACY_POLICY","version":"v1.0","agreed":true},
    {"type":"HEALTH_INFO_COLLECTION","version":"v1.0","agreed":true}
  ]'

# 4) 건강 프로필 저장
curl -X POST http://localhost:8080/api/profile \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"gender":"FEMALE","birthDate":"1985-03-01","heightCm":165,"weightKg":55,"memo":"자세 안좋음, 피로함"}'

# 4-1) AI 추천 확인 (나이/성별/메모에 따라 결과 달라짐)
curl http://localhost:8080/api/profile/recommendations -H "Authorization: Bearer $TOKEN"

# 5) 영양제 목록 조회해서 id 확인
curl http://localhost:8080/api/supplements -H "Authorization: Bearer $TOKEN"

# 6) 영양제 등록 (예: 철분 id가 1이라면) → 캐릭터 자동 배정됨
curl -X POST http://localhost:8080/api/users/me/supplements \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"supplementIds":[1]}'

# 7) 배정된 캐릭터 확인
curl http://localhost:8080/api/character/me -H "Authorization: Bearer $TOKEN"
```

## 프로젝트 구조

```
src/main/java/com/hpet/
├── HpetBackendApplication.java
├── common/                  # ApiResponse, 전역 예외 처리, EmailSender(Mock)
│   ├── email/
│   └── exception/
├── config/                  # SecurityConfig, SwaggerConfig, DataSeeder
├── filter/                  # requestId 로깅, JWT 인증 필터
├── security/                # JwtTokenProvider
├── domain/
│   ├── user/                # User, AuthProvider
│   ├── verification/        # EmailVerificationCode, PasswordResetToken
│   ├── agreement/            # UserAgreement, AgreementType
│   ├── profile/              # HealthProfile, Gender
│   ├── supplement/           # Supplement(마스터), UserSupplement
│   └── character/            # Character(마스터), UserCharacter, UserCharacterItem, GrowthStage
├── auth/                     # 회원가입/로그인/토큰/이메일인증/비번재설정 (Phase 1)
├── agreement/                 # 약관 동의 이력 API (Phase 1)
├── user/                     # /me 조회 데모 API
├── profile/                  # 건강 프로필 + AI 추천 API (Phase 2)
├── supplement/                # 영양제 검색/등록 API (Phase 2)
└── character/                 # 캐릭터 배정/조회/코스튬 API (Phase 2)
```

## 다음 단계

**Phase 3 (홈 대시보드)** 또는 **Phase 4 (복용기록·자세교정 이벤트)** 로 이어가면 됩니다.
Phase 3은 Phase 2/4 데이터를 모아 보여주는 화면이라, Phase 4를 먼저 하는 게 순서상 자연스럽습니다.
