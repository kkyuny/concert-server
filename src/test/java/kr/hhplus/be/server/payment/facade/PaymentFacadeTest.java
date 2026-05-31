package kr.hhplus.be.server.payment.facade;

import kr.hhplus.be.server.TestKafkaConfiguration;
import kr.hhplus.be.server.TestRedisConfiguration;
import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertDetailInfoResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertSeatInfoResponse;
import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.application.ConcertQueryService;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.application.PaymentCommandService;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.reservation.api.dto.ReservationChangeResponse;
import kr.hhplus.be.server.reservation.api.dto.ReservationInfoResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationQueryService;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@Import({TestRedisConfiguration.class, TestKafkaConfiguration.class})
@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {
    @Mock
    private PaymentCommandService paymentCommandService;

    @Mock
    private ReservationCommandService reservationCommandService;

    @Mock
    private ReservationQueryService reservationQueryService;

    @Mock
    private ConcertQueryService concertQueryService;

    @Mock
    private ConcertCommandService concertCommandService;

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet())
                .thenReturn(zSetOperations);
    }

    @Test
    @DisplayName("결제가 성공하면 예약/좌석 상태가 변경되고 결제 완료 이벤트가 발행된다")
    void executePaymentTest() {

        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long concertSeatId = 1L;
        Long amount = 500L;

        Reservation reservation =
                Reservation.create(userId, concertSeatId, 1L);

        given(reservationQueryService.getReservation(reservationId))
                .willReturn(ReservationInfoResponse.of(reservation));

        given(reservationCommandService.changeReservationStatus(anyLong(), any()))
                .willReturn(ReservationChangeResponse.of(reservation));

        ConcertSeat concertSeat =
                ConcertSeat.create(1L, 1);

        given(concertCommandService.changeConcertSeatStatus(anyLong(), any()))
                .willReturn(ConcertSeatStatusResponse.of(concertSeat));

        Payment payment =
                Payment.create(userId, reservationId, amount);

        given(paymentCommandService.createPayment(anyLong(), anyLong(), anyLong()))
                .willReturn(PaymentResponse.of(payment));

        // ✅ 핵심 수정 1: seatInfo 직접 값 보장
        ConcertSeatInfoResponse seatInfo =
                new ConcertSeatInfoResponse(
                        1L,   // seatId
                        10L,  // concertDetailId
                        1,
                        SeatStatus.AVAILABLE
                );

        given(concertQueryService.getConcertSeat(anyLong()))
                .willReturn(seatInfo);

        // ✅ 핵심 수정 2: detailInfo 직접 값 보장 (concertId 반드시 존재)
        ConcertDetailInfoResponse detailInfo =
                new ConcertDetailInfoResponse(
                        10L,   // concertDetailId
                        1L,    // concertId
                        500,
                        LocalDate.now()
                );

        given(concertQueryService.getConcertDetail(anyLong()))
                .willReturn(detailInfo);

        // when
        PaymentResponse result =
                paymentFacade.executePayment(reservationId, amount);

        // then
        assertThat(result.amount()).isEqualTo(amount);

        verify(eventPublisher, times(1))
                .publishEvent(any(PaymentCompletedEvent.class));

        verify(reservationQueryService)
                .getReservation(reservationId);

        verify(reservationCommandService)
                .changeReservationStatus(reservationId, ReservationStatus.CONFIRMED);

        verify(concertCommandService)
                .changeConcertSeatStatus(concertSeatId, SeatStatus.RESERVED);

        verify(paymentCommandService)
                .createPayment(userId, reservationId, amount);

        // ================= Redis 검증 =================

        verify(zSetOperations, times(1))
                .incrementScore(
                        startsWith("concert:ranking:daily:"),
                        eq("1"),   // concertId = 1L → String 변환됨
                        eq(1.0)
                );

        verify(zSetOperations, times(1))
                .incrementScore(
                        startsWith("concert:ranking:weekly:"),
                        eq("1"),
                        eq(1.0)
                );

        verify(redisTemplate, atLeastOnce())
                .expire(anyString(), any(Duration.class));
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
        Reservation expiredReservation = Reservation.create(userId, concertSeatId, 1L);
        ReservationChangeResponse changeResponse = ReservationChangeResponse.of(expiredReservation);

        given(reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.EXPIRED))
                .willReturn(changeResponse);

        // 좌석 생성 Mock
        ConcertSeat availableSeat = ConcertSeat.create(1L, 1);
        availableSeat.changeStatus(SeatStatus.AVAILABLE); // 실패 후 상태: AVAILABLE

        ConcertSeatStatusResponse seatStatusResponse =
                ConcertSeatStatusResponse.of(availableSeat);

        given(concertCommandService.changeConcertSeatStatus(concertSeatId, SeatStatus.AVAILABLE))
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
        verify(concertCommandService).changeConcertSeatStatus(concertSeatId, SeatStatus.AVAILABLE);
        verify(paymentCommandService).createFailPayment(userId, reservationId, amount);
    }
}
