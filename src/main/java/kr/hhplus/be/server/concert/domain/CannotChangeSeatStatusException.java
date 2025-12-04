package kr.hhplus.be.server.concert.domain;

public class CannotChangeSeatStatusException extends RuntimeException {
    public CannotChangeSeatStatusException(Long id) {
        super("Can't change reserved seat: " + id);
    }
}
