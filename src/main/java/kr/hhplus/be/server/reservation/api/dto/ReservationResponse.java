package kr.hhplus.be.server.reservation.api.dto;

import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;

public record ReservationResponse(Long reservationId, Long concertSeatId, ReservationStatus seatStatus) {
    public static ReservationResponse of(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getConcertSeatId(), reservation.getStatus());
    }
}
