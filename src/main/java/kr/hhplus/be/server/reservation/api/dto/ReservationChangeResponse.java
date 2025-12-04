package kr.hhplus.be.server.reservation.api.dto;

import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;

public record ReservationChangeResponse(Long reservationId, ReservationStatus reservationStatus) {
    public static ReservationChangeResponse of(Reservation reservation) {
        return new ReservationChangeResponse(reservation.getId(),  reservation.getStatus());
    }
}
