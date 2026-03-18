package kr.hhplus.be.server.reservation.facade;

import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationFacade {
    private final ReservationCommandService reservationCommandService;
    private final ConcertCommandService concertCommandService;
    private final ReservationTokenService reservationTokenService;

    @Transactional
    public ReservationResponse initReservation(Long concertSeatId, Long userId, String token, Long concertId){
        reservationTokenService.validateToken(token, userId, concertSeatId);
        concertCommandService.changeConcertSeatStatus(concertSeatId, SeatStatus.HOLD);
        return reservationCommandService.createPendingReservation(userId, concertSeatId, concertId);
    }
}
