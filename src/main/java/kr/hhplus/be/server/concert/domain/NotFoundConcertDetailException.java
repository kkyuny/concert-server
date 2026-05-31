package kr.hhplus.be.server.concert.domain;

public class NotFoundConcertDetailException extends RuntimeException {
    public NotFoundConcertDetailException(Long concertId) {
        super("Concert not found. id=" + concertId);
    }
}
