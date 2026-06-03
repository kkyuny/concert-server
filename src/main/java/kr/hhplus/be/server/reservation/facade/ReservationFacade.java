package kr.hhplus.be.server.reservation.facade;

import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.queue.application.QueueService;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.application.ReservationCommandService;
import kr.hhplus.be.server.reservation.application.ReservationTokenService;
import kr.hhplus.be.server.reservation.event.ReservationCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationFacade {
    private final ReservationCommandService reservationCommandService;
    private final ConcertCommandService concertCommandService;
    private final ReservationTokenService reservationTokenService;
    private final QueueService queueService;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationResponse reserve(Long userId, Long concertSeatId, String token) {
        boolean acquired = queueService.tryAcquire(userId);

        if (!acquired) {
            throw new IllegalStateException("대기 순서가 아닙니다.");
        }

        try {
            return initReservation(concertSeatId, userId, token);
        } finally {
            queueService.release();
        }
    }

    @Transactional
    public ReservationResponse initReservation(Long concertSeatId, Long userId, String token){

        // 1. 검증
        // TODO: 토큰 검증 로직 추가 필요함.
        /*reservationTokenService.validateToken(token, userId, concertSeatId);*/
        // 2. 좌석 HOLD
        concertCommandService.changeConcertSeatStatus(concertSeatId, SeatStatus.HOLD);
        // 3. 예약 생성
        ReservationResponse response =
                reservationCommandService.createPendingReservation(userId, concertSeatId);
        // 4. 이벤트 발행 (도메인 이벤트)
        eventPublisher.publishEvent(
                new ReservationCreatedEvent(
                        response.reservationId(),
                        userId,
                        response.concertSeatId()
                )
        );
        return response;
    }
}
