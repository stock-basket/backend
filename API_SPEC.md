# Stockbasket API 명세서

> Base URL: `https://api.stockbasket.io`  
> Content-Type: `application/json`  
> 인증: `Authorization: Bearer {access_token}` (🔒 표시 엔드포인트 필수)

---

## 공통 응답 형식

### 성공
```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

### 실패
```json
{
  "code": "USER_404",
  "message": "사용자를 찾을 수 없습니다.",
  "timestamp": "2025-04-08T12:00:00"
}
```

### 공통 에러 코드
| 코드 | HTTP | 설명 |
|---|---|---|
| `TOKEN_401` | 401 | 유효하지 않거나 만료된 토큰 |
| `VALID_400` | 400 | 요청 필드 유효성 검증 실패 |
| `SERVER_500` | 500 | 서버 내부 오류 |

---

## JWT 구조

| Claim | 타입 | 설명 |
|---|---|---|
| `userId` | `string (UUID)` | 사용자 PK |
| `role` | `string` | `ROLE_USER` \| `ROLE_ADMIN` |
| `iat` | `number` | 발급 시각 (epoch) |
| `exp` | `number` | 만료 시각 (epoch) |

---

## 1. 이메일 인증 (`/api/auth`)

### 1-1. 인증 코드 발송
```
POST /api/auth/email/send
```
이메일로 6자리 인증 코드를 발송한다.

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

---

### 1-2. 인증 코드 검증
```
POST /api/auth/email/verify
```

**Request Body**
```json
{
  "email": "user@example.com",
  "code": "483920"
}
```

**Response** `200 OK`

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `TOKEN_401` | 401 | 코드 불일치 또는 만료 |

---

## 2. 사용자 (`/api/users`)

### 2-1. 회원가입
```
POST /api/users/register
```
이메일 인증 완료 후 호출.

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Secure@123",
  "nickname": "홍길동"
}
```

> 비밀번호 규칙: 8~20자, 영문 대소문자·숫자·특수문자(`@$!%*#?&`) 조합  
> 닉네임 규칙: 2~20자

**Response** `201 Created`
```json
{ "success": true, "message": "OK", "data": null }
```

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `USER_409` | 409 | 이미 가입된 이메일 |
| `VALID_400` | 400 | 유효성 검증 실패 |

---

### 2-2. 로그인
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

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `USER_401` | 401 | 이메일 또는 비밀번호 불일치 |

---

### 2-3. 내 정보 조회 🔒
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
    "nickname": "홍길동",
    "plan": "FREE",
    "basketCount": 2
  }
}
```

---

### 2-4. 회원 탈퇴 🔒
```
DELETE /api/users/me
```

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": null }
```

---

## 3. 계정 설정 (`/api/users/me`) 🔒

### 3-1. 닉네임·이메일 변경
```
PATCH /api/users/me/account
```

**Request Body**
```json
{
  "nickname": "새닉네임",
  "email": "new@example.com"
}
```

**Response** `200 OK`

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `USER_409` | 409 | 이미 사용 중인 이메일 |

---

### 3-2. 비밀번호 변경
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

**Response** `200 OK`

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `USER_401` | 401 | 현재 비밀번호 불일치 |

---

### 3-3. 알림 설정 저장
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

**Response** `200 OK`

---

## 4. 종목 바구니 (`/api/stocks`) 🔒

### 4-1. 내 바구니 목록 조회
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
      "ticker": "005930",
      "name": "삼성전자",
      "market": "KOSPI",
      "positiveNewsCount": 5,
      "negativeNewsCount": 2,
      "neutralNewsCount": 3
    }
  ]
}
```

---

### 4-2. 바구니에 종목 추가
```
POST /api/stocks/basket
```

**Request Body**
```json
{ "ticker": "005930" }
```

**Response** `200 OK`

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `STOCK_404` | 404 | 존재하지 않는 종목 코드 |
| `STOCK_409` | 409 | 이미 바구니에 담긴 종목 |
| `STOCK_400` | 400 | 바구니 한도 초과 |

---

### 4-3. 바구니에서 종목 제거
```
DELETE /api/stocks/basket/{stockCode}
```

**Path Parameter**
| 파라미터 | 타입 | 설명 |
|---|---|---|
| `stockCode` | `string` | 종목 코드 (예: `005930`) |

**Response** `200 OK`

---

### 4-4. 종목 상세 조회
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
    "ticker": "005930",
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

### 4-5. 종목 검색
```
GET /api/stocks/search?keyword={keyword}
```

**Query Parameter**
| 파라미터 | 필수 | 설명 |
|---|---|---|
| `keyword` | ✅ | 종목명 또는 종목코드 일부 |

**Response** `200 OK` — `StockResponse[]`

---

## 5. 뉴스 피드 (`/api/news`) 🔒

### 5-1. 내 뉴스 피드 조회
```
GET /api/news[?sentiment=POSITIVE|NEGATIVE|NEUTRAL]
```

**Query Parameter**
| 파라미터 | 필수 | 설명 |
|---|---|---|
| `sentiment` | ❌ | 감성 필터. 미입력 시 전체 반환 |

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "stockCode": "005930",
      "stockName": "삼성전자",
      "title": "삼성전자, 3분기 영업이익 발표",
      "sourceName": "한국경제",
      "sourceUrl": "https://hankyung.com/...",
      "publishedAt": "2025-04-08T09:30:00",
      "sentimentType": "POSITIVE",
      "impactScore": 82,
      "aiComment": "실적 예상 상회로 단기 긍정 시그널"
    }
  ]
}
```

---

### 5-2. 뉴스 상세 조회
```
GET /api/news/{newsId}
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "stockCode": "005930",
    "stockName": "삼성전자",
    "title": "삼성전자, 3분기 영업이익 발표",
    "content": "...",
    "sourceName": "한국경제",
    "sourceUrl": "https://hankyung.com/...",
    "publishedAt": "2025-04-08T09:30:00",
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

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `NEWS_404` | 404 | 존재하지 않는 뉴스 |

---

### 5-3. 긴급 뉴스 조회
```
GET /api/news/urgent
```
내 바구니 종목의 고영향 뉴스(가격 급변동 또는 impactScore 상위).

**Response** `200 OK` — `NewsResponse[]`

---

### 5-4. 특정 종목 뉴스 조회
```
GET /api/news/stock/{stockCode}
```

**Response** `200 OK` — `NewsResponse[]`

---

## 6. 알림 (`/api/alerts`) 🔒

### 6-1. 전체 알림 목록
```
GET /api/alerts
```

**Response** `200 OK`
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 10,
      "stockName": "삼성전자",
      "alertType": "PRICE_DROP",
      "message": "삼성전자 주가 3.2% 급락",
      "priceChangeRate": -3.2,
      "isRead": false,
      "createdAt": "2025-04-08T10:15:00"
    }
  ]
}
```

**AlertType 목록**
| 값 | 설명 |
|---|---|
| `PRICE_SPIKE` | 급등 감지 |
| `PRICE_DROP` | 급락 감지 |
| `HIGH_IMPACT_NEWS` | 고영향 뉴스 |

---

### 6-2. 미읽 알림 목록
```
GET /api/alerts/unread
```

**Response** `200 OK` — `AlertResponse[]`

---

### 6-3. 미읽 알림 개수
```
GET /api/alerts/unread/count
```
네비게이션 배지 갱신용.

**Response** `200 OK`
```json
{ "success": true, "message": "OK", "data": 3 }
```

---

### 6-4. 단건 읽음 처리
```
PATCH /api/alerts/{alertId}/read
```

**Response** `200 OK`

**Error**
| 코드 | HTTP | 조건 |
|---|---|---|
| `ALERT_404` | 404 | 존재하지 않는 알림 |

---

### 6-5. 전체 읽음 처리
```
PATCH /api/alerts/read-all
```

**Response** `200 OK`

---

## 에러 코드 전체 목록

| 코드 | HTTP | 설명 |
|---|---|---|
| `TOKEN_401` | 401 | 유효하지 않거나 만료된 토큰 |
| `USER_404` | 404 | 사용자를 찾을 수 없음 |
| `USER_409` | 409 | 이미 사용 중인 이메일 |
| `USER_401` | 401 | 이메일 또는 비밀번호 불일치 |
| `STOCK_404` | 404 | 존재하지 않는 종목 |
| `STOCK_409` | 409 | 이미 바구니에 담긴 종목 |
| `STOCK_400` | 400 | 바구니 한도 초과 |
| `NEWS_404` | 404 | 존재하지 않는 뉴스 |
| `ALERT_404` | 404 | 존재하지 않는 알림 |
| `ANALYSIS_404` | 404 | 분석 결과 없음 |
| `VALID_400` | 400 | 요청 유효성 검증 실패 |
| `SERVER_500` | 500 | 서버 내부 오류 |

---

## application.yml 필수 설정

```yaml
jwt:
  secret: "your-256-bit-secret-key-minimum-32-chars!!"
  access-token-validity-ms: 3600000   # 1시간
```

---

*생성일: 2025-04-29*
