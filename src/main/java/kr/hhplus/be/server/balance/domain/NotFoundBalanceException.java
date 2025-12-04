package kr.hhplus.be.server.balance.domain;

public class NotFoundBalanceException extends RuntimeException {
    public NotFoundBalanceException(Long userId) {
        super("Balance not found. id=" + userId);
    }
}
