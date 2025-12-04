package kr.hhplus.be.server.reservation.domain;

public class NotFoundReservationException extends RuntimeException {
    public NotFoundReservationException(Long reservationId) {
        super("reservationId=" + reservationId);
    }
}
