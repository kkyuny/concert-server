package kr.hhplus.be.server.balance.infrasturcture;

import kr.hhplus.be.server.balance.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> getBalanceByUserId(Long userId);
}
