# Refactor Plan

## 목적

현재 콘서트 예약 서버를 과제 진행 순서에 맞춰 단계적으로 리팩토링한다. 비즈니스 동작은 한 번에 바꾸지 않고, 각 단계마다 현재 동작을 테스트로 고정한 뒤 구조를 개선한다.

중점 영역은 예약, 좌석, 결제, 토큰, Redis, Kafka, 예외 처리, 트랜잭션 경계이다.

## 리팩토링 이슈 순서

```text
[REFACTOR-1] 프로젝트 전체 구조 진단
[REFACTOR-2] STEP-2 서버 구조 재정리
[REFACTOR-3] STEP-3 클린 아키텍처 구조 개선
[REFACTOR-4] STEP-4 DB 정합성 및 도메인 모델 개선
[REFACTOR-5] STEP-5 동시성 제어 개선
[REFACTOR-6] STEP-6 Redis 분산락 개선
[REFACTOR-7] STEP-7 Redis 캐싱 전략 개선
[REFACTOR-8] STEP-8 이벤트 기반 구조 개선
[REFACTOR-9] STEP-9 Kafka 확장 설계 개선
[REFACTOR-10] STEP-10 장애 대응/운영 문서 개선
```

## STEP별 산출물 원칙

각 리팩토링 STEP은 코드 수정만으로 끝내지 않는다. 리팩토링 이후에는 변경된 구조와 개념, 검증 결과를 문서로 남긴다.

### 공통 산출물

- 리팩토링 내용 문서: 무엇을 왜 바꿨는지, 변경 전/후 구조와 책임 이동을 정리한다.
- 개념 정리 문서: 해당 STEP에서 사용한 개념을 과제 맥락에 맞춰 설명한다.
- 테스트 문서: 어떤 테스트를 추가/수정했고, 어떤 시나리오를 검증했는지 정리한다.
- ERD 문서: 도메인/DB 구조가 바뀐 경우 Mermaid `erDiagram`으로 갱신한다.
- Infra 문서: Redis, Kafka, scheduler, 외부 API 등 인프라 흐름이 바뀐 경우 Mermaid diagram으로 갱신한다.

### 문서 위치

권장 위치:

```text
document/refactor/
  refactor-01-structure.md
  refactor-02-server-structure.md
  refactor-03-clean-architecture.md
  refactor-04-db-domain.md
  refactor-05-concurrency.md
  refactor-06-redis-lock.md
  refactor-07-cache.md
  refactor-08-event.md
  refactor-09-kafka.md
  refactor-10-operation.md
```

기존 문서와 연결:

- ERD 변경은 `document/erd.md`에도 반영한다.
- Infra 변경은 `document/infra.md`에도 반영한다.
- Kafka 변경은 `document/kafka.md`에도 반영한다.
- 부하 테스트나 k6 시나리오 변경은 `document/load-test.md`에도 반영한다.

### Mermaid 사용 기준

- DB 구조는 `erDiagram`을 사용한다.
- 서버 계층/의존 방향은 `flowchart` 또는 `classDiagram`을 사용한다.
- 예약/결제 상태 전이는 `stateDiagram-v2`를 사용한다.
- Redis/Kafka/외부 API 흐름은 `sequenceDiagram` 또는 `flowchart`를 사용한다.

## 현재 구조 요약

- Spring Boot 3.4, Java 21 기반의 콘서트 예약 서버이다.
- 주요 도메인은 `concert`, `reservation`, `payment`, `balance`, `queue`, `user`로 분리되어 있다.
- 예약 생성 흐름은 `ReservationController -> QueueService -> ReservationFacade -> ConcertCommandService/ReservationCommandService`이다.
- 결제 흐름은 `PaymentController -> PaymentFacade -> Reservation/Concert/Payment 서비스`이며, 현재 결제 use case 안에 Redis 랭킹 갱신과 결제 완료 이벤트 발행도 함께 들어 있다.
- Redis는 좌석 락, 예약 토큰, 대기열, 콘서트 조회 캐시, 랭킹 저장소로 함께 사용된다.
- Kafka는 결제 완료 이벤트 발행 코드와 로컬 실습용 producer/consumer 코드가 production 패키지에 함께 존재한다.

## [REFACTOR-1] 프로젝트 전체 구조 진단

### 목표

- 현재 구조, 책임 경계, 위험 지점을 문서화한다. 이 단계는 문서 변경만 수행한다.

### 진단 결과

- 도메인별 패키지 분리는 되어 있으나 일부 패키지명, 계층 책임, 이벤트/Redis/Kafka 코드 위치가 섞여 있다.
- 예약/결제 facade가 여러 도메인 서비스를 조합하는 orchestration 역할을 수행한다. 방향 자체는 자연스럽지만, 일부 부가 책임이 과도하게 몰려 있다.
- 동시성 제어가 DB lock, Redis lock, Redis queue 등 여러 방식으로 실험되어 있다. 학습 목적은 좋지만 어떤 문제를 어떤 락으로 해결하는지 명확한 기준이 필요하다.
- 예외 처리와 트랜잭션 정책은 아직 일관된 규칙보다 기능 구현 중심으로 작성되어 있다.

## [REFACTOR-2] STEP-2 서버 구조 재정리

### 목표

패키지명, API 계층 책임, 실습 코드 위치를 정리한다. 기능 동작은 바꾸지 않는다.

### 예약 패키지 오타 정리

현재 `reservation.appication` 패키지명이 오타로 보인다. `reservation.application`으로 이동한다.

대상:

- `ReservationCommandService`
- `ReservationQueryService`
- `ReservationExpireService`
- `ReservationExpireScheduler`
- `ReservationTokenService`

주의:

- 패키지 이동과 import 변경만 수행한다.
- 기능 변경과 섞지 않는다.
- IDE rename/refactor 기능을 사용해 참조 누락을 방지한다.

### Controller 책임 정리

현재 예약 controller는 대기열 권한 확인과 release까지 직접 수행한다.

```java
boolean acquired = queueService.tryAcquire(request.userId());

if (!acquired) {
    throw new IllegalStateException("대기 순서가 아닙니다.");
}

try {
    return reservationFacade.initReservation(
            request.concertSeatId(),
            request.userId(),
            request.token()
    );
} finally {
    queueService.release();
}
```

문제:

- controller가 HTTP 요청/응답뿐 아니라 use case 흐름 제어까지 알고 있다.
- 예약 use case에서 대기열을 반드시 거쳐야 한다는 정책이 API 계층에 박혀 있다.
- 추후 다른 진입점, 예를 들어 내부 API나 배치에서 같은 예약 use case를 호출하면 대기열 정책이 누락될 수 있다.

개선 방향:

```java
@PostMapping
public ReservationResponse reserve(@RequestBody ReservationRequest request) {
    return reservationFacade.reserve(
            request.userId(),
            request.concertSeatId(),
            request.token()
    );
}
```

그리고 facade 또는 use case가 다음 흐름을 담당한다.

```java
public ReservationResponse reserve(Long userId, Long concertSeatId, String token) {
    queueService.acquireOrThrow(userId);

    try {
        return initReservation(concertSeatId, userId, token);
    } finally {
        queueService.release();
    }
}
```

### 실습 코드 위치 정리

Kafka 실습용 `ProducerService`, `ConsumerService`, `ProducerController`, `MemberDto`는 학습 기록으로 의미가 있다. 다만 production 코드와 같은 경로에 있으면 운영 기능처럼 보일 수 있다.

선택지:

- `sample.kafka` 패키지로 이동하고 profile을 `local` 또는 `sample`로 제한한다.
- `src/test` 또는 `document/kafka.md` 중심의 예제로 이동한다.
- 남겨두되 클래스/패키지명에 `Sample`, `Practice`, `Guide`를 명확히 붙인다.

권장:

- 과제 설명용 가이드 코드라면 유지하되 production domain Kafka와 분리한다.

## [REFACTOR-3] STEP-3 클린 아키텍처 구조 개선

### 목표

도메인/application/infrastructure 책임 경계를 선명하게 만든다.

### 예약 토큰 검증 흐름

현재 `ReservationFacade.initReservation`에서 토큰 검증이 주석 처리되어 있다.

```java
// TODO: 토큰 검증 로직 추가 필요함.
/*reservationTokenService.validateToken(token, userId, concertSeatId);*/
```

배경:

- k6 테스트 시 토큰 검증에서 "토큰 만료"가 발생해 테스트 편의를 위해 주석 처리했다.
- 하지만 실제 예약 흐름 검증에서는 토큰도 함께 검증되어야 한다.

개선 방향:

- k6 시나리오에서 토큰 발급과 예약 요청을 하나의 흐름으로 연결한다.
- 토큰 TTL 5분 안에 예약 요청이 수행되도록 시나리오를 조정한다.
- 토큰 검증 주석을 해제한다.
- 예약 성공 후 token을 consume할지 정책을 결정한다.

주의:

- 현재 토큰은 Redis lock을 실험해본 성격이 있다.
- 예약 전 Redis lock/token이 반드시 필요한 기능인지 검토한다.
- 최종 정책은 "완벽한 통제"보다 "약간의 오차 허용"에 가까우므로, 토큰은 강한 정합성 장치라기보다 선점/진입 제어 보조 장치로 문서화한다.

### ApplicationEventPublisher와 EventListener 관계

`ApplicationEventPublisher`는 이벤트를 발행하는 역할이다.

```java
eventPublisher.publishEvent(new ReservationCreatedEvent(...));
```

`@EventListener`는 발행된 이벤트를 받아 처리하는 구독자 역할이다.

```java
@EventListener
public void handle(ReservationCreatedEvent event) {
    dataPlatformClient.send(...);
}
```

문제:

- `@EventListener`는 일반적으로 이벤트 발행 시점에 동기적으로 실행된다.
- DB 트랜잭션 안에서 이벤트를 발행하면 커밋 전에 외부 API 호출이 먼저 실행될 수 있다.
- 이후 DB 트랜잭션이 rollback되면, 실제 예약은 실패했는데 외부 플랫폼에는 예약 생성 이벤트가 전송되는 불일치가 생길 수 있다.

개선 방향:

- 예약 생성 이벤트처럼 외부 시스템으로 나가는 이벤트는 `@TransactionalEventListener(phase = AFTER_COMMIT)` 사용을 검토한다.
- 결제 완료 이벤트는 이미 `@TransactionalEventListener`를 사용하고 있으므로 같은 정책으로 맞춘다.
- 외부 전송 실패 시 재시도/outbox 여부는 REFACTOR-8, REFACTOR-9에서 다룬다.

### Facade와 Application Service 경계

현재 facade가 여러 application service를 조합한다. 이 방식은 use case orchestration 역할로 볼 수 있어 자연스럽다.

다만 다음 기준을 둔다.

- facade/use case: 하나의 사용자 시나리오 흐름을 조합한다.
- command service: 단일 도메인 상태 변경을 담당한다.
- query service: 조회를 담당하고 가능하면 readOnly 트랜잭션을 사용한다.
- infrastructure: Redis, Kafka, 외부 API, JPA repository 구현 세부사항을 담당한다.

## [REFACTOR-4] STEP-4 DB 정합성 및 도메인 모델 개선

### 목표

예약, 좌석, 결제, 잔액의 상태 전이와 정합성을 도메인 모델과 트랜잭션 안에서 명확히 한다.

### 좌석 상태 전이 문제

현재 `ConcertSeat.changeStatus`는 HOLD 상태에서 AVAILABLE로 변경하는 것을 막는다.

```java
if (this.status == SeatStatus.HOLD &&
        (status == SeatStatus.AVAILABLE || status == SeatStatus.HOLD)) {
    throw new CannotChangeSeatStatusException(this.id);
}
```

그런데 예약 만료 처리에서는 HOLD 좌석을 AVAILABLE로 복구해야 한다.

```java
seat.changeStatus(SeatStatus.AVAILABLE);
```

문제:

- 예약 만료 정책과 좌석 상태 전이 규칙이 충돌한다.
- 일반 사용자 요청으로 HOLD -> AVAILABLE을 허용하지 않으려는 의도와, 시스템 만료 처리로 HOLD -> AVAILABLE을 허용하려는 의도가 구분되지 않는다.

개선 방향:

- 상태 전이를 행위 메서드로 나눈다.

```java
seat.hold();
seat.reserve();
seat.releaseHold();
```

- `releaseHold`는 예약 만료 또는 결제 실패 같은 시스템 정책에서만 호출한다.
- 상태 전이 테스트를 먼저 추가한다.

### 결제 성공 시 잔액 차감

현재 `Balance.use`는 존재하지만 결제 흐름에서 사용되지 않는다.

개선 방향:

- 결제 성공 흐름에 잔액 차감을 연결한다.
- 잔액 부족은 도메인 예외로 분리한다.
- 결제 성공 트랜잭션 안에서 다음 순서를 명확히 한다.

```text
예약 조회 및 검증
잔액 차감
예약 CONFIRMED
좌석 RESERVED
결제 CAPTURED 저장
결제 완료 이벤트 발행
```

주의:

- 현재 결제 실패 조건은 "예약 상태가 PENDING이어야 결제 가능"이라는 정책이다.
- `reservationInfoResponse.seatStatus()`라는 이름은 실제 의미가 예약 상태라면 `reservationStatus()`로 변경하는 것이 좋다.

### 예약 만료 스케줄러 정합성

현재 만료 스케줄러는 만료 대상 예약을 조회한 뒤 예약 상태와 좌석 상태를 변경한다.

문제:

- 같은 예약이 결제 중일 때 만료 스케줄러도 동시에 실행될 수 있다.
- 예약 상태 변경에는 lock이 있지만, 만료 대상 목록 조회와 좌석 복구 경계가 명확하지 않다.

개선 방향:

- 만료 대상 조회 시 예약 row lock 또는 상태 조건부 update를 검토한다.
- 결제와 만료가 동시에 같은 예약을 처리할 때 하나만 성공하도록 테스트를 추가한다.
- 좌석 복구는 예약이 실제로 PENDING -> EXPIRED 전이에 성공한 경우에만 수행한다.

## [REFACTOR-5] STEP-5 동시성 제어 개선

### 목표

DB pessimistic lock, Redis lock, Redis queue가 각각 어떤 문제를 해결하는지 분리하고 테스트로 검증한다.

### Redis 분산락과 DB pessimistic lock을 함께 쓰는 문제

현재 좌석 상태 변경은 Redis 분산락과 DB pessimistic lock을 함께 사용한다.

이 자체가 항상 잘못은 아니다. 다만 두 락의 목적이 다르다.

- DB pessimistic lock: 같은 DB row를 동시에 수정하지 못하게 막는다.
- Redis 분산락: DB 접근 전에 애플리케이션 여러 인스턴스 사이의 진입을 제한한다.

문제:

- 두 락을 함께 쓰면 정합성은 강해질 수 있지만 복잡도가 증가한다.
- Redis 락 획득 후 DB 트랜잭션이 길어지면 락 TTL과 실제 작업 시간이 어긋날 수 있다.
- 어떤 락이 핵심 정합성을 보장하는지 불명확하면 장애 분석이 어려워진다.

개선 방향:

- 좌석 중복 예약 방지의 최종 정합성은 DB lock 또는 DB unique/상태 조건으로 보장한다.
- Redis lock은 부하 감소 또는 빠른 실패를 위한 보조 장치인지 명확히 문서화한다.
- 한 PR에서 락 방식을 바꾸지 말고, 먼저 현재 동시성 테스트를 강화한다.

### 대기열의 약간의 오차 허용 정책

현재 `RedisQueueRepository`의 `tryAcquire` 흐름은 여러 Redis 명령으로 나뉘어 있어 완전 원자적이지 않다.

사용자 정책:

- 대기열은 완벽한 정합성보다 약간의 오차를 허용한다.

정리:

- 이 정책이면 Lua script까지 강제할 필요는 낮다.
- 다만 active count가 음수가 되는 문제는 정책과 별개로 방지해야 한다.

개선 방향:

- `release` 시 active count가 0 아래로 내려가지 않도록 보정한다.
- 동시성 테스트에서 active count가 음수가 되지 않는지만 검증한다.
- 대기열 오차 허용 범위를 문서화한다.

## [REFACTOR-6] STEP-6 Redis 분산락 개선

### 목표

Redis lock/token/key 정책을 정리한다.

### Redis 락 키와 TTL 분리

현재 `ConcertCommandService` 내부에 좌석 lock key와 TTL이 직접 정의되어 있다.

```java
String lockKey = "seat:" + concertSeatId + ":lock";
private static final long LOCK_EXPIRE_SEC = 5;
```

문제:

- 다른 곳에서 같은 정책을 재사용하기 어렵다.
- 테스트에서 key를 맞추기 어렵다.
- TTL 변경 이유가 service 코드 안에 묻힌다.

개선 방향:

- key 생성은 `RedisKeys` 같은 key factory로 분리한다.
- lock 획득/해제는 `SeatLockManager` 또는 `RedisSeatLockRepository`로 분리한다.

예시:

```java
public final class RedisKeys {
    public static String seatLock(Long seatId) {
        return "concert:seat:%d:lock".formatted(seatId);
    }
}
```

```java
public interface SeatLockManager {
    LockToken acquire(Long seatId);
    void release(LockToken token);
}
```

### Lua script 미사용 정책

현재 락 해제는 value를 비교한 뒤 delete한다.

```java
String currentValue = redisTemplate.opsForValue().get(lockKey);
if (lockValue.equals(currentValue)) {
    redisTemplate.delete(lockKey);
}
```

Lua script를 쓰면 비교와 삭제를 Redis 서버에서 원자적으로 처리할 수 있다.

다만 현재 과제에서는 Redis lock이 학습/보조 장치이고, 완벽한 통제보다 약간의 오차를 허용하는 정책이다. 따라서 Lua script 도입은 필수가 아니라 개선 후보로 둔다.

정리:

- 지금은 compare-and-delete 유지 가능.
- 추후 운영 수준 정합성이 필요하면 Lua script 또는 Redisson 도입 검토.

### 토큰 저장 포맷 수정

현재 발급은 `userId + ":" + seatId`로 저장하고, 검증은 `split("-")`로 파싱한다.

개선:

```java
String[] split = value.split(":");
```

추가로 `System.out.println` 디버그 출력은 제거한다.

토큰 예외:

- 토큰은 Redis lock 실습 성격이 있으므로 처음부터 과도한 예외 체계를 만들 필요는 없다.
- 그래도 API 응답 구분을 위해 만료, 불일치, 선점 실패 정도는 별도 예외로 분리하는 것이 좋다.

## [REFACTOR-7] STEP-7 Redis 캐싱 전략 개선

### 목표

Redis cache, ranking, key naming, TTL 정책을 정리한다.

### 캐시 키 관리 방식 혼재

현재 조회 캐시는 `@Cacheable`을 사용한다.

```java
@Cacheable(cacheNames = "availableSeats", key = "'availableSeats:' + #concertDetailId")
```

반면 좌석 캐시 무효화는 Redis key 문자열을 직접 삭제한다.

```java
redisTemplate.delete("seatStatus::seat:" + concertSeatId);
```

문제:

- Spring Cache는 내부적으로 cache name과 key를 조합해 Redis key를 만든다.
- 직접 문자열 삭제 방식은 실제 Spring Cache key와 어긋날 수 있다.
- 캐시 prefix, serializer, key generator 설정이 바뀌면 직접 delete 코드가 깨질 수 있다.

관련 Redis/Spring Cache 개념:

- Redis key: Redis에 저장되는 실제 문자열 key.
- TTL: key가 자동 만료되는 시간.
- Cache name: Spring Cache가 논리적으로 구분하는 캐시 영역.
- Cache key: 메서드 인자를 기반으로 만든 캐시 식별자.
- Cache eviction: 데이터 변경 후 오래된 캐시를 제거하는 작업.

개선 방향:

- 캐시 무효화는 `@CacheEvict` 또는 `CacheManager`를 우선 사용한다.
- 직접 Redis key 삭제가 필요하면 Spring Cache key 생성 규칙과 동일한 key factory를 둔다.
- 콘서트 목록, 콘서트 날짜, 예약 가능 좌석, 랭킹별 TTL을 분리한다.

### Redis 키 네이밍 정책

현재 Redis key가 여러 클래스에 흩어져 있다.

개선 방향:

```text
concert:seat:{seatId}:lock
concert:seat:{seatId}:token
reservation:token:{token}
reservation:queue
reservation:queue:users
reservation:active
concert:ranking:daily:{yyyy-MM-dd}
concert:ranking:weekly:{yyyy}-W{week}
```

효과:

- 운영 중 Redis key를 보고 용도를 파악하기 쉽다.
- 테스트 초기화 범위를 잡기 쉽다.
- TTL과 key 생명주기를 문서화하기 쉽다.

### 랭킹 시스템 분리

현재 일간/주간 랭킹 Redis 업데이트가 `PaymentFacade.executePayment` 내부에 있다.

개선 방향:

- `ConcertRankingService`로 분리한다.
- 결제 성공 후 직접 호출하거나, `PaymentCompletedEvent` 리스너에서 갱신한다.
- 미사용 상수 `DAILY_TTL`, `WEEKLY_TTL`, `getYearWeek`는 정리한다.

권장 흐름:

```text
PaymentFacade: 결제 성공 이벤트 발행
PaymentCompletedEventListener: 랭킹 갱신
ConcertRankingService: Redis ZSET 업데이트
```

## [REFACTOR-8] STEP-8 이벤트 기반 구조 개선

### 목표

예약/결제 이벤트 발행 시점과 외부 side effect 경계를 정리한다.

### 예약 생성 이벤트

현재 예약 생성 이벤트는 `@EventListener`로 처리된다.

개선 방향:

- DB 커밋 이후 외부 전송이 필요하면 `@TransactionalEventListener(phase = AFTER_COMMIT)`로 변경한다.
- 외부 전송 실패가 예약 트랜잭션을 실패시키지 않도록 분리한다.
- 실패 로그는 `printStackTrace`가 아니라 logger를 사용한다.

### 결제 완료 이벤트

현재 결제 완료 이벤트는 `@TransactionalEventListener`로 Kafka 발행된다. 방향은 좋다.

보완:

- phase를 명시해 의도를 드러낸다.
- Kafka 발행 실패 시 재시도 또는 outbox 도입을 검토한다.
- 결제 완료 이벤트를 랭킹 갱신에도 활용할지 결정한다.

### 트랜잭션과 side effect

DB 트랜잭션 안에서 Redis/Kafka/외부 API 호출을 모두 섞으면 다음 문제가 생길 수 있다.

- DB rollback 이후 외부 이벤트는 이미 전송됨.
- 외부 API 지연으로 DB transaction이 길어짐.
- Kafka 발행 실패 때문에 결제 DB 저장까지 실패시킬지 정책이 불명확함.

정책:

- 핵심 DB 상태 변경은 하나의 트랜잭션으로 묶는다.
- 외부 전송은 커밋 이후 이벤트로 분리한다.
- 실패한 외부 전송은 로그/재시도/outbox로 다룬다.

## [REFACTOR-9] STEP-9 Kafka 확장 설계 개선

### 목표

Kafka 발행 구조를 운영 코드와 실습 코드로 분리하고, 장애 대응 확장 지점을 만든다.

### Outbox 패턴

현재 Kafka 발행 실패는 `e.printStackTrace()`로 처리된다. 이 경우 메시지 유실 여부를 추적하기 어렵다.

Outbox 패턴은 다음 흐름이다.

```text
1. 비즈니스 트랜잭션 안에서 DB 상태 변경
2. 같은 트랜잭션 안에서 outbox 테이블에 이벤트 저장
3. 별도 publisher가 outbox 이벤트를 Kafka로 발행
4. 발행 성공 시 outbox 상태를 SENT로 변경
5. 실패 시 재시도 또는 DLQ 처리
```

장점:

- DB 변경과 이벤트 저장의 원자성을 보장할 수 있다.
- Kafka 장애가 있어도 이벤트를 나중에 재발행할 수 있다.
- 운영 중 어떤 이벤트가 실패했는지 추적 가능하다.

적용 계획:

- 바로 완성형 outbox를 도입하기보다 payment complete 이벤트부터 작은 outbox 테이블로 시작한다.
- 발행 상태 `PENDING`, `SENT`, `FAILED`를 둔다.
- 재시도 스케줄러 또는 배치 publisher를 둔다.

### Kafka 토픽명 설정화

현재 토픽명이 하드코딩되어 있다.

```java
"payment-complete-topic"
```

개선:

- `application.yml`에 토픽명을 둔다.
- configuration properties로 주입한다.

예시:

```yaml
app:
  kafka:
    topics:
      payment-completed: payment-complete-topic
```

### 실습용 Consumer 유지 여부

로컬 실습용 consumer는 학습 기록과 가이드 코드로 의미가 있다. 다만 운영 코드처럼 자동 실행되면 혼란이 생긴다.

개선 방향:

- `@Profile("local-kafka-practice")`로 제한한다.
- 패키지를 `global.kafka.sample`처럼 분리한다.
- `document/kafka.md`에 실습 목적을 명시한다.

## [REFACTOR-10] STEP-10 장애 대응/운영 문서 개선

### 목표

예외 응답, 트랜잭션 정책, Redis/Kafka 장애 대응, 운영 문서를 정리한다.

### 예외 처리 개선

현재 `ApiControllerAdvice`는 모든 `Exception`을 500으로 처리한다.

문제:

- 클라이언트 요청이 잘못된 경우도 서버 오류처럼 보인다.
- 예: 토큰 만료, 잔액 부족, 좌석 선점 실패, 잘못된 상태 전이 등은 대부분 400 또는 409에 가깝다.
- 모니터링에서 실제 서버 장애와 비즈니스 실패를 구분하기 어렵다.

개선 방향:

- `BusinessException` 기반 구조를 만든다.
- 각 예외에 `errorCode`, `HttpStatus`, `message`를 둔다.
- `ProblemDetail`에 `errorCode`, `domain`, `timestamp`, `path`를 추가한다.

수정 대상:

- `NotFoundConcertDetailException`, `NotFoundConcertSeatException` 핸들러 파라미터 타입 수정.
- `CannotChangeSeatStatusException` 매핑 추가.
- 토큰 만료/불일치/선점 실패 매핑 추가.
- 잔액 부족 예외 매핑 추가.
- `IllegalArgumentException`은 400, 상태 충돌성 `IllegalStateException`은 가능하면 도메인 예외로 대체한다.

### 트랜잭션 중첩 정책

현재 facade와 command service 양쪽에 `@Transactional`이 있다.

중첩 자체가 항상 문제는 아니다. Spring 기본 전파 옵션 `REQUIRED`에서는 이미 트랜잭션이 있으면 같은 트랜잭션에 참여한다.

문제는 "어디가 트랜잭션 경계인지" 읽기 어려워지는 것이다.

- facade가 전체 use case 트랜잭션을 담당하는지
- command service가 각각 독립 트랜잭션을 담당하는지
- 이벤트 발행과 외부 side effect가 어느 트랜잭션에 묶이는지

개선 정책:

- facade/use case가 여러 도메인 변경을 조합하면 facade에 write transaction을 둔다.
- command service는 단일 도메인 변경 메서드로 유지하되, 독립 호출 가능성이 있는 경우에만 transaction을 둔다.
- query service는 `@Transactional(readOnly = true)`로 통일한다.
- 이벤트 리스너는 커밋 이후 처리 여부를 명시한다.

### 운영 문서화 항목

문서화할 내용:

- 예약 토큰 TTL과 k6 테스트 시나리오 주의사항.
- 대기열은 약간의 오차를 허용한다는 정책.
- 좌석 중복 예약의 최종 정합성은 어떤 락/DB 정책으로 보장하는지.
- Redis key naming과 TTL.
- Kafka topic, key, payload schema.
- Kafka 발행 실패 시 outbox/DLQ/재시도 정책.
- 예약 만료 스케줄러 실행 주기와 결제 동시 실행 시 기대 결과.
- 각 STEP별 리팩토링 내용, 관련 개념, 테스트 결과.
- Mermaid 기반 ERD, infra diagram, 이벤트/상태 전이 diagram.

## 리팩토링 원칙

- 한 PR에서는 하나의 STEP만 다룬다.
- 리팩토링 전 현재 동작을 테스트로 고정한다.
- 리팩토링 후에는 변경 내용, 관련 개념, 테스트 결과를 문서로 남긴다.
- ERD와 infra 흐름이 바뀌면 Mermaid diagram을 함께 갱신한다.
- 기능 변경이 필요한 경우 문서에 "정책 변경"으로 명시한다.
- Redis/Kafka/외부 API 같은 side effect는 DB 핵심 트랜잭션과 분리하는 방향으로 정리한다.
- 실습용 코드는 production runtime에 포함하지 않는다. 필요한 학습 기록은 문서, 테스트, 샘플 코드로 분리한다.
- 동시성 관련 변경은 단위 테스트보다 통합 테스트와 k6 시나리오를 우선한다.
