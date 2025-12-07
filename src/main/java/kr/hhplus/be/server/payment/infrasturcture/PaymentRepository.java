package kr.hhplus.be.server.payment.infrasturcture;

import kr.hhplus.be.server.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
