package kr.hhplus.be.server.reservation.facade;

import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationFacade {
    private final ReservationCommandService reservationCommandService;
    private final ConcertCommandService concertCommandService;

    @Transactional
    public ReservationResponse initReservation(Long concertSeatId, Long userId){
        concertCommandService.changeConcertSeatStaus(concertSeatId, SeatStatus.HOLD);
        return reservationCommandService.createPendingReservation(userId, concertSeatId);
    }
}
