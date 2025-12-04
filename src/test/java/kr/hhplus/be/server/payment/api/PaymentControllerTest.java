package kr.hhplus.be.server.payment.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.payment.api.dto.PaymentRequest;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.facade.PaymentFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PaymentFacade paymentFacade;

    @Test
    void requestPaymentTest() throws Exception {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        Long amount = 500L;

        PaymentRequest paymentRequest = new PaymentRequest(reservationId, amount);
        Payment payment = Payment.create(userId, reservationId, amount);
        PaymentResponse paymentResponse = PaymentResponse.of(payment);

        when(paymentFacade.executePayment(paymentRequest.reservationId(), paymentRequest.amount()))
                .thenReturn(paymentResponse);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(paymentResponse.amount()));

        verify(paymentFacade).executePayment(paymentRequest.reservationId(), paymentRequest.amount());
    }

}