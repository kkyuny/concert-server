package kr.hhplus.be.server.balance.application;

import kr.hhplus.be.server.balance.api.dto.BalanceChargeRequest;
import kr.hhplus.be.server.balance.api.dto.BalanceChargeResponse;
import kr.hhplus.be.server.balance.domain.Balance;
import kr.hhplus.be.server.balance.infrasturcture.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BalanceCommandServiceTest {
    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private BalanceCommandService balanceCommandService;

    @Test
    void chargeBalanceTest() {
        // given
        Long userId = 1L;
        Long amount = 100L;
        Balance balance = Balance.create(userId); // 0원으로 생성

        BalanceChargeRequest request = new BalanceChargeRequest(userId, amount);

        given(balanceRepository.getBalanceByUserId(userId))
                .willReturn(Optional.of(balance));

        // when
        BalanceChargeResponse response = balanceCommandService.chargeBalance(request.userId(), request.amount());

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.balance()).isEqualTo(balance.getBalance());
        assertThat(response.balance()).isEqualTo(100L); // 0 + 100
    }

}