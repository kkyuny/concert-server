package kr.hhplus.be.server.payment.application;

import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.infrasturcture.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class PaymentCommandService {
    private final PaymentRepository paymentRepository;

    public PaymentResponse createPayment(Long userId, Long reservationId, Long amount) {
        Payment payment = Payment.create(userId,reservationId,amount);

        return PaymentResponse.of(paymentRepository.save(payment));
    }

    public PaymentResponse createFailPayment(Long userId, Long reservationId, Long amount) {
        Payment payment = Payment.createFail(userId,reservationId,amount);

        return PaymentResponse.of(paymentRepository.save(payment));
    }
}
