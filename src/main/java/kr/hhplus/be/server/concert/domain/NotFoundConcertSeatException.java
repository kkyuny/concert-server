package kr.hhplus.be.server.concert.domain;

public class NotFoundConcertSeatException extends RuntimeException {
    public NotFoundConcertSeatException(Long id) {
        super("ConcertSeat not found. id=" + id);
    }
}
