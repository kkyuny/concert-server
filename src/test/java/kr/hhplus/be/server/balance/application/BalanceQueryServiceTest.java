package kr.hhplus.be.server.balance.application;

import kr.hhplus.be.server.balance.api.dto.BalanceSearchResponse;
import kr.hhplus.be.server.balance.domain.Balance;
import kr.hhplus.be.server.balance.domain.NotFoundBalanceException;
import kr.hhplus.be.server.balance.infrasturcture.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BalanceQueryServiceTest {
    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private BalanceQueryService balanceQueryService;

    @Test
    void getBalance() {
        Long userId = 1L;
        Balance balance = Balance.create(userId);

        given(balanceRepository.getBalanceByUserId(userId))
                .willReturn(Optional.of(balance));

        BalanceSearchResponse balanceSearchResponse = balanceQueryService.getBalance(userId);

        assertThat(balanceSearchResponse.userId()).isEqualTo(balance.getUserId());
        assertThat(balanceSearchResponse.balance()).isEqualTo(balance.getBalance());

    }

    @Test
    void getBalance_fail() {
        Long userId = 1L;
        Balance balance = Balance.create(2L);

        given(balanceRepository.getBalanceByUserId(userId))
                .willThrow(NotFoundBalanceException.class);

        // when & then
        assertThatThrownBy(() -> balanceQueryService.getBalance(userId))
            .isInstanceOf(NotFoundBalanceException.class);
    }
}