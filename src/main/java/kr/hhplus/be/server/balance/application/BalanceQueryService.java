package kr.hhplus.be.server.balance.application;

import kr.hhplus.be.server.balance.api.dto.BalanceSearchResponse;
import kr.hhplus.be.server.balance.domain.Balance;
import kr.hhplus.be.server.balance.domain.NotFoundBalanceException;
import kr.hhplus.be.server.balance.infrasturcture.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BalanceQueryService {
    private final BalanceRepository balanceRepository;

    public BalanceSearchResponse getBalance(Long userId) {
        Balance balance = balanceRepository.getBalanceByUserId(userId)
                .orElseThrow(()-> new NotFoundBalanceException(userId));

        return BalanceSearchResponse.of(balance);
    }
}
