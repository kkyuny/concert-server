package kr.hhplus.be.server.reservation.domain;

public class CannotChangeReservationStatusException extends RuntimeException {
    public CannotChangeReservationStatusException(Long id) {
        super("Can't change reservation status for id: " + id);
    }
}
