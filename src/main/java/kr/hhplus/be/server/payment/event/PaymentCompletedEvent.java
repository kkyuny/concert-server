package kr.hhplus.be.server.payment.event;

public record PaymentCompletedEvent(
        Long reservationId,
        Long userId,
        Long concertId,
        Long amount
) {}
