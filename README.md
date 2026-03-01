# 🎫 콘서트 예약 서비스
> 대기열 + 좌석 임시배정 + 포인트 충전식 결제 기반의 콘서트 예약 서비스.

- STEP 05: 동시성 제어
1. 좌석 임시 배정 시 락 제어
✅ 문제 상황
- 여러 사용자가 동시에 같은 좌석을 예약 시도
- 둘 이상의 트랜잭션이 동시에 좌석 상태를 조회하면 모두 AVAILABLE로 판단 가능
- 결과적으로 중복 예약 발생 가능성

✅ 해결 전략
- 비관적 락 적용(**@Lock(LockModeType.PESSIMISTIC_WRITE)**)
- 좌석 조회 시점에 좌석정보를 concertSeatRepository.findByIdForLock(concertSeatId)로 획득
- 하나의 트랜잭션만 해당 좌석 수정 가능

🔎 대안으로 고려했던 방법
조건부 업데이트 (Optimistic한 방식)
```
update concert_seat
set status = 'PENDING'
where id = ? and status = 'AVAILABLE'
```
- 성공 row count == 1 이면 성공
- 0이면 이미 선점된 것
📌 개인적으로는 조건부 업데이트 방식이 성능상 더 유리하다고 생각함.

✅ 테스트 결과
- 10개 동시 요청 테스트 수행
- 오직 1건만 좌석 선점 성공
- 나머지 요청 9건 실패 확인

2. 잔액 차감 동시성 제어
✅ 문제 상황
- 사용자가 동시에 여러 결제 요청 시도
- 동일 사용자 잔액을 동시에 조회 후 차감
- 잔액이 중복해서 내려가는 문제 발생 가능

✅ 해결 전략
- 비관적 락 적용(**@Lock(LockModeType.PESSIMISTIC_WRITE)**)
- 결제 시작 시점에 예약정보를 getReservationWithLock(reservationId)로 획득
- 하나의 트랜잭션만 잔액 수정 가능
- 상태 기반 검증
```
  if (reservation.getStatus() != PENDING) {
    return paymentCommandService.createFailPayment();
  }
```
- 오직 PENDING 상태에서만 결제 가능
- 첫 번째 트랜잭션이 예약상태를 CONFIRMED로 변경하면 이후 요청은 즉시 실패

✅ 테스트 결과
- 10개 동시 요청 테스트 수행
- 오직 1건만 좌석 선점 성공
- 나머지 요청 9건 실패 확인

3. 배정 타임아웃 해제 스케줄러
✅ 문제 상황
- 좌석을 PENDING 상태로 임시 배정
- 사용자가 결제하지 않고 이탈
- 좌석이 계속 점유된 상태로 남음

✅ 해결 전략
- 스케줄러 기반 만료 처리
- 주기적으로 PENDING + expiredAt <= now 조회
- 상태 → EXPIRED
- 좌석 상태 → AVAILABLE 복구
```
@Scheduled(fixedRate = 60000)
public void run() {
reservationExpireService.expireReservations();
}
```
✅ 테스트 전략
- 스케줄러는 직접 테스트하지 않고 만료 서비스 단위 테스트 수행
- expiredAt 지난 예약 데이터 생성
- expireReservations() 실행
- 상태 변경 및 좌석 복구 검증

✅ 테스트 결과
- 만료 대상인 row EXPIRED 변경
- 좌석 상태 AVAILABLE 복구

📌 전체 설계 요약

| 항목 | 문제 | 해결 전략 | 결과 |
|------|------|-----------|------|
| 좌석 배정 | 동일 좌석에 대한 중복 예약 | 비관적 락(PESSIMISTIC_WRITE)<br>*(대안: 조건부 업데이트 쿼리 가능)* | 중복 예약 방지 성공 |
| 잔액 차감 | 동일 예약에 대한 중복 결제 발생 가능 | 비관적 락 | 결제 1회만 성공 (중복 방지) |
| 타임아웃 해제 | 결제 미완료 시 좌석 점유 고착 | 스케줄러 + 만료 상태(EXPIRED) 전환 + 좌석 반환 | 자원 정상 회수 |