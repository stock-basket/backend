# Stockbasket API 명세서

> **Base URL** `https://api.stockbasket.io`  
> **Content-Type** `application/json` (SSE 엔드포인트 제외)  
> **인증** 🔒 표시 엔드포인트는 `Authorization: Bearer {access_token}` 헤더 필수

---

## 목차

1. [공통 사항](#1-공통-사항)
2. [이메일 인증 `/api/auth`](#2-이메일-인증-apiauth)
3. [회원 `/api/users`](#3-회원-apiusers)
4. [계정·알림 설정 `/api/users/me`](#4-계정알림-설정-apiusersme--🔒)
5. [종목 바구니 `/api/stocks`](#5-종목-바구니-apistocks--🔒)
6. [뉴스 피드 `/api/news`](#6-뉴스-피드-apinews--🔒)
7. [알림 `/api/alerts`](#7-알림-apialerts--🔒)
8. [실시간 차트 `/api/chart`](#8-실시간-차트-apichart--🔒)
9. [에러 코드 전체 목록](#9-에러-코드-전체-목록)

---

## 1. 공통 사항

### 성공 응답
```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```
`data`가 없는 경우 `null`.

### 실패 응답
```json
{
  "code": "USER_404",
  "message": "사용자를 찾을 수 없습니다.",
  "timestamp": "2026-05-07T09:30:00"
}
```

### 페이지네이션 응답 (`Page<T>`)
뉴스 목록처럼 페이지네이션이 적용된 엔드포인트는 `data` 안이 아래 구조:
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [ ... ],
    "page": {
      "size": 10,
      "number": 0,
      "totalElements": 47,
      "totalPages": 5
    }
  }
}
```
**공통 페이지 쿼리 파라미터**

| 파라미터 | 기본값 | 설명 |
|---|---|---|
| `page` | `0` | 페이지 번호 (0-indexed) |
| `size` | 엔드포인트마다 다름 | 페이지 크기 |
| `sort` | — | 정렬 (예: `publishedAt,desc`) |

### Access Token 구조 (JWT Claims)

| Claim | 타입 | 설명 |
|---|---|---|
| `userId` | `string (UUID)` | 사용자 PK |
| `role` | `string` | `ROLE_USER` \| `ROLE_ADMIN` |
| `iat` | `number` | 발급 시각 (epoch) |
| `exp` | `number` | 만료 시각 (epoch) |

---

## 2. 이메일 인증 (`/api/auth`)

> 인증 불필요 (whitelist)

### 회원가입 이메일 인증 플로우

```
[1] POST /api/auth/email/send      → 이메일로 6자리 코드 발송
[2] POST /api/auth/email/verify    → 코드 검증 → verifiedToken 반환
[3] POST /api/users/register       → verifiedToken을 X-Verified-Token 헤더로 전달
```

---

### 2-1. 인증 코드 발송

```
POST /api/auth/email/send
```

**Request Body**
```json
{ "email": "user@example.com" }
```

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `USER_409` | 409 | 이미 가입된 이메일 |
| `VALID_400` | 400 | 이메일 형식 오류 |

---

### 2-2. 인증 코드 검증

```
POST /api/auth/email/verify
```

코드가 일치하면 **verifiedToken** (JWT 문자열, 유효기간 **10분**)을 반환한다.  
이 토큰은 회원가입 요청 시 `X-Verified-Token` 헤더로 사용한다.  
`Authorization: Bearer ...` 형식이 **아님** — 헤더 이름 그대로 토큰 문자열만 전달.

**Request Body**
```json
{
  "email": "user@example.com",
  "code": "483920"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```
`data` = verifiedToken 문자열

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `TOKEN_401` | 401 | 코드 불일치 또는 만료 |

---

## 3. 회원 (`/api/users`)

### 3-1. 회원가입

```
POST /api/users/register
```

이메일 인증(`/api/auth/email/verify`)을 먼저 완료해야 한다.  
인증 완료 시 받은 verifiedToken을 `X-Verified-Token` 헤더로 전달.

**Headers**
```
X-Verified-Token: eyJhbGciOiJIUzI1NiJ9...   ← verifiedToken (Bearer 없이 문자열 그대로)
Content-Type: application/json
```

**Request Body**
```json
{
  "password": "Secure@123",
  "nickname": "홍길동",
  "newsletterAlert": true,
  "volatilityAlert": false
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `password` | `string` | ✅ | 8~20자, 영문 대소문자·숫자·특수문자(`@$!%*#?&`) |
| `nickname` | `string` | ✅ | 2~20자 |
| `newsletterAlert` | `boolean` | ✅ | 뉴스·호재 알림 수신 동의 |
| `volatilityAlert` | `boolean` | ✅ | 급등락 이메일 알림 수신 동의 |

> **이메일은 verifiedToken에서 추출** — 요청 바디에 포함하지 않는다.

> `newsletterAlert` / `volatilityAlert` 동의 값에 따라 서버가 UserSetting 초기값을 자동 설정한다.

**Response** `201 Created`
```json
{ "success": true, "message": "OK", "data": null }
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `TOKEN_401` | 401 | verifiedToken 누락, 만료(10분 초과), 또는 위변조 |
| `USER_409` | 409 | 이미 가입된 이메일 |
| `VALID_400` | 400 | 비밀번호·닉네임 유효성 실패 |

---

### 3-2. 로그인

```
POST /api/users/login
```

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Secure@123"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```
이후 모든 🔒 엔드포인트에 `Authorization: Bearer {accessToken}` 헤더로 사용한다.

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `USER_401` | 401 | 이메일 또는 비밀번호 불일치 |
| `VALID_400` | 400 | 이메일 형식 오류 또는 필드 누락 |

---

### 3-3. 내 정보 조회 🔒

```
GET /api/users/me
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "nickname": "홍길동"
  }
}
```

---

### 3-4. 회원 탈퇴 🔒

```
DELETE /api/users/me
```

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

---

## 4. 계정·알림 설정 (`/api/users/me`) 🔒

### 4-1. 닉네임 변경

```
PATCH /api/users/me/account
```

**Request Body**
```json
{ "nickname": "새닉네임" }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `nickname` | `string` | ✅ | 2~20자 |

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `VALID_400` | 400 | 닉네임 유효성 실패 |

---

### 4-2. 비밀번호 변경

```
PATCH /api/users/me/password
```

**Request Body**
```json
{
  "currentPassword": "OldPass@1",
  "newPassword": "NewPass@2"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `currentPassword` | `string` | ✅ | 현재 비밀번호 |
| `newPassword` | `string` | ✅ | 새 비밀번호 (비밀번호 규칙 동일) |

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `USER_401` | 401 | 현재 비밀번호 불일치 |

---

### 4-3. 알림 설정 변경

```
PATCH /api/users/me/alert-settings
```

**Request Body**
```json
{
  "isGlobalAlertEnabled": true,
  "isVolatilityAlertEnabled": true,
  "volatilityThresholdPercent": 3.0,
  "isBadNewsAlertEnabled": true,
  "isGoodNewsAlertEnabled": false,
  "newsImpactThreshold": 70,
  "isEmailAlertEnabled": true
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `isGlobalAlertEnabled` | `boolean` | 전체 알림 ON/OFF (master switch) |
| `isVolatilityAlertEnabled` | `boolean` | 급등락 가격 알림 ON/OFF |
| `volatilityThresholdPercent` | `number` | 급등락 알림 임계값 (%) — 예: `3.0` → 3% 이상 변동 시 알림 |
| `isBadNewsAlertEnabled` | `boolean` | 악재 뉴스 알림 ON/OFF |
| `isGoodNewsAlertEnabled` | `boolean` | 호재 뉴스 알림 ON/OFF |
| `newsImpactThreshold` | `integer` | 뉴스 영향도 알림 최소 임계값 (0~100) |
| `isEmailAlertEnabled` | `boolean` | 이메일 알림 ON/OFF |

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

---

## 5. 종목 바구니 (`/api/stocks`) 🔒

### 5-1. 내 바구니 목록 조회

```
GET /api/stocks/basket
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "stockCode": "005930",
      "name": "삼성전자",
      "market": "KOSPI",
      "positiveNewsCount": 5,
      "negativeNewsCount": 2,
      "neutralNewsCount": 3
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `stockCode` | `string` | 종목 코드 (예: `005930`) |
| `name` | `string` | 종목명 |
| `market` | `string` | `"KOSPI"` \| `"KOSDAQ"` |
| `positiveNewsCount` | `integer` | 긍정 뉴스 수 |
| `negativeNewsCount` | `integer` | 부정 뉴스 수 |
| `neutralNewsCount` | `integer` | 중립 뉴스 수 |

---

### 5-2. 바구니에 종목 추가

```
POST /api/stocks/basket
```

**Request Body**
```json
{ "stockCode": "005930" }
```

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `STOCK_404` | 404 | 존재하지 않는 종목 코드 |
| `STOCK_409` | 409 | 이미 바구니에 담긴 종목 |
| `STOCK_400` | 400 | 바구니 한도 초과 (최대 20개) |

---

### 5-3. 바구니에서 종목 제거

```
DELETE /api/stocks/basket/{stockCode}
```

**Path Parameter**
| 파라미터 | 타입 | 설명 |
|---|---|---|
| `stockCode` | `string` | 종목 코드 (예: `005930`) |

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `STOCK_404` | 404 | 바구니에 없는 종목 |

---

### 5-4. 종목 상세 조회

```
GET /api/stocks/{stockCode}
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "stockCode": "005930",
    "name": "삼성전자",
    "market": "KOSPI",
    "positiveNewsCount": 5,
    "negativeNewsCount": 2,
    "neutralNewsCount": 3
  }
}
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `STOCK_404` | 404 | 존재하지 않는 종목 |

---

### 5-5. 종목 검색

```
GET /api/stocks/search?keyword={keyword}
```

**Query Parameter**
| 파라미터 | 필수 | 설명 |
|---|---|---|
| `keyword` | ✅ | 종목명 또는 종목코드 일부 (대소문자 무시) |

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "stockCode": "005930",
      "name": "삼성전자",
      "market": "KOSPI",
      "positiveNewsCount": 0,
      "negativeNewsCount": 0,
      "neutralNewsCount": 0
    }
  ]
}
```

---

## 6. 뉴스 피드 (`/api/news`) 🔒

### 뉴스 응답 공통 필드

> `sentimentType`, `impactScore`, `aiComment`가 `null` 또는 `0`이면 AI 분석이 아직 진행 중인 상태.

### 6-1. 내 뉴스 피드 조회

```
GET /api/news
```

내 바구니 종목의 뉴스를 최신순으로 반환한다.

**Query Parameter**
| 파라미터 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `sentiment` | ❌ | — | 감성 필터: `POSITIVE` \| `NEGATIVE` \| `NEUTRAL` |
| `page` | ❌ | `0` | 페이지 번호 |
| `size` | ❌ | `10` | 페이지 크기 |

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": 1,
        "stockCode": "005930",
        "stockName": "삼성전자",
        "title": "삼성전자, 3분기 영업이익 시장 예상 상회",
        "publisher": "한국경제",
        "sourceUrl": "https://hankyung.com/article/...",
        "publishedAt": "2026-05-07T09:30:00",
        "sentimentType": "POSITIVE",
        "impactScore": 82,
        "aiComment": "실적 예상 상회로 단기 긍정 시그널"
      }
    ],
    "page": {
      "size": 10,
      "number": 0,
      "totalElements": 47,
      "totalPages": 5
    }
  }
}
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `NEWS_400` | 400 | 잘못된 `sentiment` 값 |

---

### 6-2. 뉴스 상세 조회

```
GET /api/news/{newsId}
```

AI 분석 전체 결과를 포함한 상세 응답.

**Path Parameter**
| 파라미터 | 타입 | 설명 |
|---|---|---|
| `newsId` | `long` | 뉴스 ID |

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "stockCode": "005930",
    "stockName": "삼성전자",
    "title": "삼성전자, 3분기 영업이익 시장 예상 상회",
    "content": "삼성전자가 올해 3분기 영업이익으로...",
    "publisher": "한국경제",
    "sourceUrl": "https://hankyung.com/article/...",
    "publishedAt": "2026-05-07T09:30:00",
    "sentimentType": "POSITIVE",
    "impactScore": 82,
    "marketShockScore": 65,
    "reliabilityScore": 90,
    "shortTermImpact": 78,
    "longTermImpact": 55,
    "aiAnalysis": "삼성전자의 3분기 영업이익이 시장 예상치를 15% 상회...",
    "aiVerdict": "단기 매수 관점 유효. 중장기 불확실성 존재."
  }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `sentimentType` | `string\|null` | `POSITIVE` \| `NEGATIVE` \| `NEUTRAL` — null이면 분석 중 |
| `impactScore` | `integer` | 종합 영향도 (0~100) — 0이면 분석 중 |
| `marketShockScore` | `integer` | 시장 충격 지수 (0~100) |
| `reliabilityScore` | `integer` | 뉴스 신뢰도 (0~100) |
| `shortTermImpact` | `integer` | 단기 영향 (0~100) |
| `longTermImpact` | `integer` | 장기 영향 (0~100) |
| `aiAnalysis` | `string\|null` | AI 분석 전문 |
| `aiVerdict` | `string\|null` | AI 한 줄 판단 |

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `NEWS_404` | 404 | 존재하지 않는 뉴스 |

---

### 6-3. 긴급 뉴스 조회

```
GET /api/news/urgent
```

내 바구니 종목 중 `impactScore` 80 이상인 고영향 뉴스.

**Query Parameter**
| 파라미터 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `page` | ❌ | `0` | 페이지 번호 |
| `size` | ❌ | `5` | 페이지 크기 |

**Response** `200 OK` — 뉴스 Page 응답 (6-1과 동일한 `content` 구조)

---

### 6-4. 특정 종목 뉴스 조회

```
GET /api/news/stock/{stockCode}
```

**Path Parameter**
| 파라미터 | 타입 | 설명 |
|---|---|---|
| `stockCode` | `string` | 종목 코드 (예: `005930`) |

**Query Parameter**
| 파라미터 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `page` | ❌ | `0` | 페이지 번호 |
| `size` | ❌ | `10` | 페이지 크기 |

**Response** `200 OK` — 뉴스 Page 응답 (6-1과 동일한 `content` 구조)

---

## 7. 알림 (`/api/alerts`) 🔒

### 알림 응답 공통 필드

```json
{
  "id": 10,
  "stockName": "삼성전자",
  "alertType": "PRICE_SPIKE",
  "message": "삼성전자 주가가 5.08% 급등했습니다.",
  "priceChangeRate": 5.08,
  "isRead": false,
  "createdAt": "2026-05-07T10:15:00"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `long` | 알림 ID |
| `stockName` | `string` | 종목명 |
| `alertType` | `string` | `PRICE_SPIKE` \| `PRICE_DROP` \| `HIGH_IMPACT_NEWS` |
| `message` | `string` | 알림 메시지 |
| `priceChangeRate` | `number\|null` | 가격 변동률 (%) — 뉴스 알림이면 null |
| `isRead` | `boolean` | 읽음 여부 |
| `createdAt` | `string` | ISO-8601 datetime |

---

### 7-1. 전체 알림 목록 조회

```
GET /api/alerts
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": [ { ...알림 응답... } ]
}
```

---

### 7-2. 미읽 알림 목록 조회

```
GET /api/alerts/unread
```

**Response** `200 OK` — 알림 배열 (7-1과 동일한 구조)

---

### 7-3. 미읽 알림 개수 조회

```
GET /api/alerts/unread/count
```

네비게이션 배지 갱신용.

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": 3 }
```

---

### 7-4. 단건 읽음 처리

```
PATCH /api/alerts/{alertId}/read
```

**Path Parameter**
| 파라미터 | 타입 | 설명 |
|---|---|---|
| `alertId` | `long` | 알림 ID |

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

---

### 7-5. 전체 읽음 처리

```
PATCH /api/alerts/read-all
```

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

---

## 8. 실시간 차트 (`/api/chart`) 🔒

### 8-1. 실시간 가격 SSE 스트림

```
GET /api/chart/{stockCode}/stream
```

> **Content-Type**: `text/event-stream` (SSE — Server-Sent Events)  
> 연결을 끊으면 구독도 해제된다.

**Path Parameter**
| 파라미터 | 타입 | 설명 |
|---|---|---|
| `stockCode` | `string` | 종목 코드 (예: `005930`) |

**동작**
- 해당 종목이 Kiwoom 웹소켓에서 수신 중이 아니면 자동으로 구독 시작
- 접속 시점의 현재가를 `snapshot` 이벤트로 즉시 전송 (현재가 없으면 생략)
- 이후 Kiwoom에서 새 틱이 들어올 때마다 `tick` 이벤트 전송 (초당이 아닌 틱 단위)
- 장 외 시간에 접속하면 `snapshot` 이벤트 이후 `tick` 이벤트가 오지 않음

**이벤트 형식**

접속 직후 현재가 즉시 전송:
```
event: snapshot
data: {"stockCode":"005930","price":72300,"time":"2026-05-07T09:30:00.123Z"}
```

이후 틱마다:
```
event: tick
data: {"stockCode":"005930","price":72350,"time":"2026-05-07T09:30:01.456Z"}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `stockCode` | `string` | 종목 코드 |
| `price` | `integer` | 현재가 (원) |
| `time` | `string` | ISO-8601 UTC 타임스탬프 |

**JavaScript 예시**
```javascript
const es = new EventSource('/api/chart/005930/stream', {
  headers: { Authorization: `Bearer ${accessToken}` }
});

es.addEventListener('snapshot', e => {
  const { price } = JSON.parse(e.data);
  console.log('현재가:', price);
});

es.addEventListener('tick', e => {
  const { price, time } = JSON.parse(e.data);
  chart.update(price, time);
});

es.onerror = () => es.close();
```

> **1분봉 저장**: 서버는 수신된 틱을 분 단위 OHLC 캔들로 집계해 DB에 저장한다.  
> 과거 캔들 조회 API는 추후 추가 예정.

---

## 9. 에러 코드 전체 목록

| 코드 | HTTP | 설명 |
|---|---|---|
| `TOKEN_401` | 401 | 유효하지 않거나 만료된 토큰 |
| `EXPIRED_TOKEN` | 401 | 토큰 만료 |
| `INVALID_TOKEN` | 401 | 위변조 또는 형식 오류 토큰 |
| `USER_404` | 404 | 사용자를 찾을 수 없음 |
| `USER_409` | 409 | 이미 사용 중인 이메일 |
| `USER_401` | 401 | 이메일 또는 비밀번호 불일치 |
| `STOCK_404` | 404 | 존재하지 않는 종목 |
| `STOCK_409` | 409 | 이미 바구니에 담긴 종목 |
| `STOCK_400` | 400 | 바구니 한도 초과 (최대 20개) |
| `NEWS_404` | 404 | 존재하지 않는 뉴스 |
| `NEWS_400` | 400 | 잘못된 sentiment 값 |
| `VALID_400` | 400 | 요청 유효성 검증 실패 (상세 내용은 message에 포함) |
| `SERVER_500` | 500 | 서버 내부 오류 |

---

## 부록 — 회원가입 전체 플로우

```
1. POST /api/auth/email/send        { email }
   → 이메일로 6자리 코드 발송

2. POST /api/auth/email/verify      { email, code }
   → 성공 시 verifiedToken (JWT, 10분 유효) 반환

3. POST /api/users/register
   Header: X-Verified-Token: {verifiedToken}
   Body:   { password, nickname, newsletterAlert, volatilityAlert }
   → 201 Created
```

---

*최종 수정: 2026-05-07*
