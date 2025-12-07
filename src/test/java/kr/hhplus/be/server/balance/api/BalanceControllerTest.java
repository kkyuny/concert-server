package kr.hhplus.be.server.balance.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.balance.api.dto.BalanceChargeRequest;
import kr.hhplus.be.server.balance.api.dto.BalanceChargeResponse;
import kr.hhplus.be.server.balance.api.dto.BalanceSearchResponse;
import kr.hhplus.be.server.balance.application.BalanceCommandService;
import kr.hhplus.be.server.balance.application.BalanceQueryService;
import kr.hhplus.be.server.balance.domain.Balance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    private BalanceQueryService balanceQueryService;
    @MockitoBean
    private BalanceCommandService balanceCommandService;

    @Test
    void getBalanceTest() throws Exception {
        Long userId = 1L;
        Balance balance = Balance.create(userId);
        BalanceSearchResponse balanceSearchResponse = BalanceSearchResponse.of(balance);

        when(balanceQueryService.getBalance(userId)).thenReturn(balanceSearchResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/balances/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(balanceSearchResponse.balance()))
                .andExpect(jsonPath("$.userId").value(balanceSearchResponse.userId()));
    }

    @Test
    void chargeBalanceTest() throws Exception {
        Long userId = 1L;
        Long amount = 100L;
        Balance balance = Balance.create(userId);
        BalanceChargeRequest balanceChargeRequest = new BalanceChargeRequest(userId, amount);
        String requestToString = objectMapper.writeValueAsString(balanceChargeRequest);
        BalanceChargeResponse  balanceChargeResponse = BalanceChargeResponse.of(balance);

        when(balanceCommandService.chargeBalance(balanceChargeRequest.userId(), balanceChargeRequest.amount())).thenReturn(balanceChargeResponse);

        mockMvc.perform(post("/api/balances/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestToString))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance").value(balanceChargeResponse.balance()))
                .andExpect(jsonPath("$.userId").value(balanceChargeResponse.userId()));
    }
}