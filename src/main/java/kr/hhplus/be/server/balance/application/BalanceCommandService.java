package kr.hhplus.be.server.balance.application;

import kr.hhplus.be.server.balance.api.dto.BalanceChargeRequest;
import kr.hhplus.be.server.balance.api.dto.BalanceChargeResponse;
import kr.hhplus.be.server.balance.api.dto.BalanceSearchResponse;
import kr.hhplus.be.server.balance.domain.Balance;
import kr.hhplus.be.server.balance.domain.NotFoundBalanceException;
import kr.hhplus.be.server.balance.infrasturcture.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class BalanceCommandService {
    private final BalanceRepository balanceRepository;

    public BalanceChargeResponse chargeBalance(Long userId, Long amount) {
        Balance balance = balanceRepository.getBalanceByUserId(userId)
                .orElseThrow(() -> new NotFoundBalanceException(userId));

        balance.charge(amount);

        return BalanceChargeResponse.of(balance);
    }
}
