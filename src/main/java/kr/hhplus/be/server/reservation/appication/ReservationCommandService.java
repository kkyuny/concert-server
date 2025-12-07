package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.reservation.api.dto.ReservationChangeResponse;
import kr.hhplus.be.server.reservation.api.dto.ReservationRequest;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.domain.NotFoundReservationException;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrasturcture.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class ReservationCommandService {
    private final ReservationRepository reservationRepository;

    public ReservationResponse createPendingReservation(Long userId, Long concertSeatId) {
        Reservation reservation = Reservation.create(concertSeatId, userId);
        return ReservationResponse.of(reservationRepository.save(reservation));

    }

    public ReservationChangeResponse changeReservationStatus(Long reservationId, ReservationStatus reservationStatus) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundReservationException(reservationId));

        return ReservationChangeResponse.of(reservation.changeStatus(reservationStatus));
    }
}
