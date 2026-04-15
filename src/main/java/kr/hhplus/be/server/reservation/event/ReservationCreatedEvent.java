package kr.hhplus.be.server.reservation.event;

public record ReservationCreatedEvent(
        Long reservationId,
        Long userId,
        Long concertId,
        Long seatId
) {}