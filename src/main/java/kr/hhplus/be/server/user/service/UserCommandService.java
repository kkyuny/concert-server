package kr.hhplus.be.server.user.service;

import kr.hhplus.be.server.balance.domain.Balance;
import kr.hhplus.be.server.balance.infrasturcture.BalanceRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserCommandService {
    private UserRepository userRepository;
    private BalanceRepository balanceRepository;

    @Transactional
    public Long registerUser(Long userId) {
        User user = User.create();
        User savedUser = userRepository.save(user);

        Balance balance = Balance.create(savedUser.getId());
        balanceRepository.save(balance);

        return savedUser.getId();
    }
}
