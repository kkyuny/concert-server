package kr.hhplus.be.server.balance.api.dto;

import kr.hhplus.be.server.balance.domain.Balance;

public record BalanceChargeResponse(Long userId, Long balance) {
    public static BalanceChargeResponse of(Balance balance) {
        return new BalanceChargeResponse(balance.getUserId(), balance.getBalance());
    }
}
