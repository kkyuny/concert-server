package kr.hhplus.be.server.reservation.facade;

import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationTokenService;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReservationResponse initReservation(Long concertSeatId, Long userId, String token, Long concertId){
        // 1. 검증
        reservationTokenService.validateToken(token, userId, concertSeatId);

        // 2. 좌석 HOLD
        concertCommandService.changeConcertSeatStatus(concertSeatId, SeatStatus.HOLD);

        // 3. 예약 생성
        ReservationResponse response =
                reservationCommandService.createPendingReservation(userId, concertSeatId, concertId);

        // 4. 이벤트 발행 (도메인 이벤트)
        eventPublisher.publishEvent(
                new ReservationCreatedEvent(
                        response.reservationId(),
                        userId,
                        concertId,
                        concertSeatId
                )
        );

        return response;
    }
}
