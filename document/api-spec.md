## API 명세서 (요약)
상세 OpenAPI 명세는 `./document/openapi.yaml`에서 확인해주세요.

## 개요
- **`/api/concerts`** : 콘서트 조회 관련 API
    - **`/dates`** : 예약 가능 날짜 조회
    - **`/seats?date=...`** : 지정 날짜의 좌석 조회

- **`/api/reservations`** : 좌석 예약 관련 API
    - **POST** : 좌석 임시 예약 요청

- **`/api/balances`** : 포인트 관련 API
    - **POST `/charge`** : 포인트 충전
    - **GET `/{userId}`** : 사용자 잔액 조회

- **`/api/payments`** : 결제 처리 API
    - **POST** : 결제 요청

## 콘서트 조회(`/api/concerts`)

### ✅ 예약 가능한 날짜 조회
**GET /api/concerts/dates**

**Response**
```json
{
  "dates": ["2025-11-15", "2025-11-16"]
}
```

### ✅ 예약 가능한 좌석 조회
**GET /api/concerts/seats?date={date}**

**Response**
```json
{
  "seats": ["1", "2", "3", "..."]
}
```

## 좌석 예약 (`/api/reservations`)

### ✅ 좌석 예약 요청
**POST /api/reservations**

**Request**
```json
{
  "date": "2025-11-15",
  "seatNo": 10,
  "userId": 100
}
```

**Response**
```json
{
  "result": "SUCCESS"
}
```

## 포인트 잔액(`/api/balances`)
### ✅ 포인트 잔액 조회
**GET /api/balances/{userId}**

**Response**
```json
{
  "balance": 120000
}
```

### ✅ 포인트 충전
**POST /api/balances/charge**

**Request**
```json
{
  "userId": 1,
  "amount": 50000
}
```

**Response**
```json
{
  "balance": "150000"
}
```

## 결제 처리(`/api/payments`)

### ✅ 결제 요청
**POST /api/payments**

**Request**
```json
{
  "userId": 100,
  "concertDetailId": 202,
  "reservationId": 5001,
  "amount": 300000
}
```

**Response**
```json
{
  "result": "결제 성공"
}
```

## Request / Response DTO

| Schema | 설명 |
|---|---|
| ReservationRequest | 예약 생성 요청 |
| ReservationResponse | 예약 결과 |
| RequestPaymentRequest | 결제 요청 정보 |
| RequestPaymentResponse | 결제 결과 |
| BalanceChargeRequest | 포인트 충전 요청 |
| BalanceChargeResponse | 잔액 정보 |
| ConcertSeatsRequest | 좌석 요청 조건 |
| ConcertSeatsResponse | 가용 좌석 목록 |
| ConcertDatesResponse | 예약 가능 날짜 목록 |
| BalanceSearchResponse | 잔액 조회 결과 |

## 비고
- 비즈니스 정책에 따라 확장 예정
