package kr.hhplus.be.server.reservation.api.dto;

import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationInfoResponse(
        Long reservationId,
        Long userId,
        Long concertSeatId,
        ReservationStatus seatStatus,
        LocalDateTime expiredAt
) {
    public static ReservationInfoResponse of(Reservation reservation) {
        return new ReservationInfoResponse(reservation.getId(),
                reservation.getUserId(),
                reservation.getConcertSeatId(),
                reservation.getStatus(),
                reservation.getExpiredAt());
    }
}
