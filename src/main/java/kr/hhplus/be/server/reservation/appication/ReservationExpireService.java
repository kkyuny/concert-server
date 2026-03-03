package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrastructure.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationExpireService {

    private final ReservationRepository reservationRepository;
    private final ConcertSeatRepository concertSeatRepository;

    @Transactional
    public void expireReservations() {

        List<Reservation> targets =
                reservationRepository.findExpiredPending(LocalDateTime.now());

        for (Reservation reservation : targets) {
            // 상태 변경
            reservation.changeStatus(ReservationStatus.EXPIRED);

            // 좌석 복구
            concertSeatRepository.findById(reservation.getConcertSeatId())
                    .ifPresent(seat ->
                            seat.changeStatus(SeatStatus.AVAILABLE)
                    );
        }
    }
}
