package kr.hhplus.be.server.balance.api;

import kr.hhplus.be.server.balance.api.dto.BalanceChargeRequest;
import kr.hhplus.be.server.balance.api.dto.BalanceChargeResponse;
import kr.hhplus.be.server.balance.application.BalanceCommandService;
import kr.hhplus.be.server.balance.application.BalanceQueryService;
import kr.hhplus.be.server.balance.domain.Balance;
import kr.hhplus.be.server.balance.api.dto.BalanceSearchResponse;
import kr.hhplus.be.server.balance.infrasturcture.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/balances")
public class BalanceController {
    private final BalanceCommandService balanceCommandService;
    private final BalanceQueryService balanceQueryService;

    @GetMapping("/{userId}")
    public BalanceSearchResponse getBalance(@PathVariable Long userId){
        return balanceQueryService.getBalance(userId);
    }

    @PostMapping("/charge")
    public BalanceChargeResponse chargeBalance(@RequestBody BalanceChargeRequest request) {
        return balanceCommandService.chargeBalance(request.userId(), request.amount());
    }
}
