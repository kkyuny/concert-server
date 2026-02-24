package kr.hhplus.be.server.balance.infrastructure;

import kr.hhplus.be.server.balance.api.dto.BalanceChargeRequest;
import kr.hhplus.be.server.balance.api.dto.BalanceChargeResponse;
import kr.hhplus.be.server.balance.application.BalanceCommandService;
import kr.hhplus.be.server.balance.application.BalanceQueryService;
import kr.hhplus.be.server.balance.domain.Balance;
import kr.hhplus.be.server.balance.domain.NotFoundBalanceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BalanceIntegrationTest {

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private BalanceCommandService balanceCommandService;

    @Autowired
    private BalanceQueryService balanceQueryService;

    @Test
    void chargeBalanceTest() {
        // given: 유저 생성
        Long userId = 1L;
        Balance balance = Balance.create(userId);
        balanceRepository.save(balance);

        Long amount = 100L;
        BalanceChargeRequest request = new BalanceChargeRequest(userId, amount);

        // when: 충전
        BalanceChargeResponse response = balanceCommandService.chargeBalance(request.userId(), request.amount());

        // then: DB와 반환값 검증
        Balance updatedBalance = balanceRepository.getBalanceByUserId(userId).orElseThrow();

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.balance()).isEqualTo(100L);
        assertThat(updatedBalance.getBalance()).isEqualTo(100L);
    }

    @Test
    void findByUserId_notFound() {
        Long userId = 999L;

        assertThatThrownBy(() ->
                balanceQueryService.getBalance(userId))
            .isInstanceOf(NotFoundBalanceException.class);
    }
}
