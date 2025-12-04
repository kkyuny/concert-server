package kr.hhplus.be.server.payment.api.dto;

import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.domain.PaymentStatus;

public record PaymentResponse(Long paymentId, Long amount, PaymentStatus paymentStatus) {
    public static PaymentResponse of(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getAmount(),  payment.getStatus());
    }
}
