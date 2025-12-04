package kr.hhplus.be.server.payment.application;

import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.infrasturcture.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class PaymentCommandServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentCommandService paymentCommandService;

    @Test
    void createPaymentTest() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        Long amount = 500L;

        Payment payment = Payment.create(userId, reservationId, amount);

        given(paymentRepository.save(any(Payment.class)))
                .willReturn(payment);

        // when
        PaymentResponse saved = paymentCommandService.createPayment(userId, reservationId, amount);

        // then
        assertThat(saved.paymentId()).isEqualTo(payment.getId());
        assertThat(saved.amount()).isEqualTo(payment.getAmount());
        assertThat(saved.paymentStatus()).isEqualTo(payment.getStatus());
    }

    @Test
    void createFailPaymentTest() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        Long amount = 500L;

        Payment payment = Payment.createFail(userId, reservationId, amount);

        given(paymentRepository.save(any(Payment.class)))
                .willReturn(payment);

        // when
        PaymentResponse saved = paymentCommandService.createFailPayment(userId, reservationId, amount);

        // then
        assertThat(saved.paymentId()).isEqualTo(payment.getId());
        assertThat(saved.paymentStatus()).isEqualTo(payment.getStatus());
        assertThat(saved.amount()).isEqualTo(payment.getAmount());
    }

}
