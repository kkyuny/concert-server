package kr.hhplus.be.server.payment.facade;

import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.application.PaymentCommandService;
import kr.hhplus.be.server.reservation.api.dto.ReservationInfoResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationQueryService;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PaymentFacade {
    private final PaymentCommandService paymentCommandService;
    private final ConcertCommandService concertCommandService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    @Transactional
    public PaymentResponse executePayment(Long reservationId, Long amount) {
        ReservationInfoResponse reservationInfoResponse = reservationQueryService.getReservation(reservationId);

        // 결제 실패
        if (reservationInfoResponse.expiredAt().isBefore(LocalDateTime.now())) {
            reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.EXPIRED);
            concertCommandService.changeConcertSeatStaus(reservationInfoResponse.concertSeatId(), SeatStatus.AVAILABLE);

            return paymentCommandService.createFailPayment(reservationInfoResponse.userId(), reservationId, amount);
        }

        // 결제 성공
        reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.CONFIRMED);
        concertCommandService.changeConcertSeatStaus(reservationInfoResponse.concertSeatId(), SeatStatus.RESERVED);

        return paymentCommandService.createPayment(reservationInfoResponse.userId(), reservationId, amount);
    }
}
