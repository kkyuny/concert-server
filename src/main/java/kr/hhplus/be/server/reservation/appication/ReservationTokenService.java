package kr.hhplus.be.server.reservation.appication;

public interface ReservationTokenService {
    String issueToken(Long userId, Long seatId);

    void validateToken(String token, Long userId, Long seatId);

    void consumeToken(String token, Long seatId);
}
