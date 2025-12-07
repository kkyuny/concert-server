package kr.hhplus.be.server.payment.api;

import kr.hhplus.be.server.payment.api.dto.PaymentRequest;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.facade.PaymentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentFacade paymentFacade;

    @PostMapping
    public PaymentResponse requestPayment(@RequestBody PaymentRequest request){
        return paymentFacade.executePayment(request.reservationId(), request.amount());
    }
}
