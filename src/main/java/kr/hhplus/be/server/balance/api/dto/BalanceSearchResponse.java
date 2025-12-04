package kr.hhplus.be.server.balance.api.dto;

import kr.hhplus.be.server.balance.domain.Balance;

public record BalanceSearchResponse(Long userId, Long balance) {
    public static BalanceSearchResponse of(Balance balance) {
        return new BalanceSearchResponse(balance.getUserId(), balance.getBalance());
    }
}
