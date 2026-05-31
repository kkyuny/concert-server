package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.NotFoundConcertSeatException;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import kr.hhplus.be.server.reservation.api.dto.ReservationChangeResponse;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.domain.NotFoundReservationException;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrastructure.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class ReservationCommandService {
    private final ReservationRepository reservationRepository;
    private final ConcertSeatRepository concertSeatRepository;

    public ReservationResponse createPendingReservation(Long userId, Long concertSeatId) {
        ConcertSeat seat = concertSeatRepository.findById(concertSeatId)
                .orElseThrow(() -> new NotFoundConcertSeatException(concertSeatId));
        Reservation reservation = Reservation.create(userId, concertSeatId, seat.getConcertDetailId());

        return ReservationResponse.of(reservationRepository.save(reservation));
    }

    public ReservationChangeResponse changeReservationStatus(Long reservationId, ReservationStatus reservationStatus) {
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId).orElseThrow(
                () -> new NotFoundReservationException(reservationId));

        return ReservationChangeResponse.of(reservation.changeStatus(reservationStatus));
    }
}
