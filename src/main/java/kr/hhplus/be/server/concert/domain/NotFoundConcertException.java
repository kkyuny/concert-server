package kr.hhplus.be.server.concert.domain;

public class NotFoundConcertException extends RuntimeException {
    public NotFoundConcertException(Long concertId) {
        super("Concert not found. id=" + concertId);
    }
}
