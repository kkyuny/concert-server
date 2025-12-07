package kr.hhplus.be.server.payment.facade;

import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.application.PaymentCommandService;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.reservation.api.dto.ReservationChangeResponse;
import kr.hhplus.be.server.reservation.api.dto.ReservationInfoResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationQueryService;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {
    @Mock
    private PaymentCommandService paymentCommandService;

    @Mock
    private ReservationCommandService reservationCommandService;

    @Mock
    private ReservationQueryService reservationQueryService;

    @Mock
    private ConcertCommandService concertCommandService;

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Test
    void executePaymentTest() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long concertSeatId = 1L;
        Long amount = 500L;

        // 1) 예약 정보 조회 Mock
        Reservation reservation = Reservation.create(userId, concertSeatId);
        ReservationInfoResponse reservationInfoResponse = ReservationInfoResponse.of(reservation);
        given(reservationQueryService.getReservation(reservationId))
                .willReturn(reservationInfoResponse);

        // 2) 예약 상태 변경 Mock
        ReservationChangeResponse reservationChangeResponse = ReservationChangeResponse.of(reservation);
        given(reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.CONFIRMED))
                .willReturn(reservationChangeResponse);

        // 3) 좌석 상태 변경 Mock
        ConcertSeat concertSeat = ConcertSeat.create(1L, 1);
        ConcertSeatStatusResponse concertSeatStatusResponse = ConcertSeatStatusResponse.of(concertSeat);
        given(concertCommandService.changeConcertSeatStaus(concertSeatId, SeatStatus.RESERVED))
                .willReturn(concertSeatStatusResponse);

        // 4) 결제 생성 Mock
        Payment payment = Payment.create(userId, reservationId, amount);
        PaymentResponse paymentResponse = PaymentResponse.of(payment);
        given(paymentCommandService.createPayment(userId, reservationId, amount))
                .willReturn(paymentResponse);

        // when
        PaymentResponse result = paymentFacade.executePayment(reservationId, amount);

        // then
        assertThat(result.paymentId()).isEqualTo(payment.getId());
        assertThat(result.amount()).isEqualTo(payment.getAmount());

        verify(reservationQueryService).getReservation(reservationId);
        verify(reservationCommandService).changeReservationStatus(reservationId, ReservationStatus.CONFIRMED);
        verify(concertCommandService).changeConcertSeatStaus(concertSeatId, SeatStatus.RESERVED);
        verify(paymentCommandService).createPayment(userId, reservationId, amount);
    }

    @Test
    void executePayment_fail() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long concertSeatId = 1L;
        Long amount = 500L;

        ReservationInfoResponse expiredInfo = new ReservationInfoResponse(
                reservationId,
                userId,
                concertSeatId,
                ReservationStatus.PENDING,
                LocalDateTime.now().minusMinutes(1) // 만료 처리
        );

        given(reservationQueryService.getReservation(reservationId))
                .willReturn(expiredInfo);

        // 예약 생성 Mock
        Reservation expiredReservation = Reservation.create(userId, concertSeatId);
        ReservationChangeResponse changeResponse = ReservationChangeResponse.of(expiredReservation);

        given(reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.EXPIRED))
                .willReturn(changeResponse);

        // 좌석 생성 Mock
        ConcertSeat availableSeat = ConcertSeat.create(1L, 1);
        availableSeat.changeStatus(SeatStatus.AVAILABLE); // 실패 후 상태: AVAILABLE

        ConcertSeatStatusResponse seatStatusResponse =
                ConcertSeatStatusResponse.of(availableSeat);

        given(concertCommandService.changeConcertSeatStaus(concertSeatId, SeatStatus.AVAILABLE))
                .willReturn(seatStatusResponse);

        // 실패 결제 생성 Mock
        Payment failPayment = Payment.createFail(userId, reservationId, amount);
        PaymentResponse failPaymentResponse = PaymentResponse.of(failPayment);

        given(paymentCommandService.createFailPayment(userId, reservationId, amount))
                .willReturn(failPaymentResponse);

        // when
        PaymentResponse result = paymentFacade.executePayment(reservationId, amount);

        // then
        assertThat(result.paymentId()).isEqualTo(failPayment.getId());
        assertThat(result.paymentStatus()).isEqualTo(failPayment.getStatus());
        assertThat(result.amount()).isEqualTo(failPayment.getAmount());

        verify(reservationQueryService).getReservation(reservationId);
        verify(reservationCommandService).changeReservationStatus(reservationId, ReservationStatus.EXPIRED);
        verify(concertCommandService).changeConcertSeatStaus(concertSeatId, SeatStatus.AVAILABLE);
        verify(paymentCommandService).createFailPayment(userId, reservationId, amount);
    }
}
