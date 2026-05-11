package kr.hhplus.be.server.payment.facade;

import kr.hhplus.be.server.TestKafkaConfiguration;
import kr.hhplus.be.server.TestRedisConfiguration;
import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.application.ConcertCommandService;
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
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

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
    private ConcertCommandService concertCommandService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @BeforeEach
    void setup() {
        // opsForZSet()는 final이라 willReturn() 못 씀
        lenient().doReturn(zSetOperations).when(redisTemplate).opsForZSet();
    }

    @Test
    @DisplayName("결제가 성공하면 예약/좌석 상태가 변경되고 결제 완료 이벤트가 발행된다")
    void executePaymentTest() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long concertSeatId = 1L;
        Long amount = 500L;

        // 1) 예약 정보 Mock
        Reservation reservation = Reservation.create(userId, concertSeatId, 1L);
        given(reservationQueryService.getReservation(reservationId))
                .willReturn(ReservationInfoResponse.of(reservation));

        // 2) 예약 상태 변경 Mock
        given(reservationCommandService.changeReservationStatus(anyLong(), any()))
                .willReturn(ReservationChangeResponse.of(reservation));

        // 3) 좌석 상태 변경 Mock
        ConcertSeat concertSeat = ConcertSeat.create(1L, 1);
        given(concertCommandService.changeConcertSeatStatus(anyLong(), any()))
                .willReturn(ConcertSeatStatusResponse.of(concertSeat));

        // 4) 결제 생성 Mock
        Payment payment = Payment.create(userId, reservationId, amount);
        given(paymentCommandService.createPayment(anyLong(), anyLong(), anyLong()))
                .willReturn(PaymentResponse.of(payment));

        // when
        PaymentResponse result = paymentFacade.executePayment(reservationId, amount);

        // then
        assertThat(result.paymentId()).isEqualTo(payment.getId());
        assertThat(result.amount()).isEqualTo(amount);

        // 이벤트 발행 검증 -> PaymentCompletedEvent 타입의 객체가 한 번 발행되었는지 확인
        verify(eventPublisher, times(1)).publishEvent(any(PaymentCompletedEvent.class));

        // 기존 로직 검증
        verify(reservationQueryService).getReservation(reservationId);
        verify(reservationCommandService).changeReservationStatus(eq(reservationId), eq(ReservationStatus.CONFIRMED));
        verify(concertCommandService).changeConcertSeatStatus(concertSeatId, SeatStatus.RESERVED);
        verify(paymentCommandService).createPayment(eq(userId), eq(reservationId), eq(amount));
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
                LocalDateTime.now().minusMinutes(1), // 만료 처리
                1L
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
