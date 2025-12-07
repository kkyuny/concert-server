package kr.hhplus.be.server.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Payment extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long userId;
    private long reservationId;
    private long amount;
    private PaymentStatus status;

    public static Payment create(Long userId, Long reservationId, Long amount) {
        Payment payment = new Payment();
        payment.userId = userId;
        payment.reservationId = reservationId;
        payment.amount = amount;
        payment.status = PaymentStatus.CAPTURED;

        return payment;
    }

    public static Payment createFail(Long userId, Long reservationId, Long amount) {
        Payment payment = new Payment();
        payment.userId = userId;
        payment.reservationId = reservationId;
        payment.amount = amount;
        payment.status = PaymentStatus.FAILED;

        return payment;
    }
}
