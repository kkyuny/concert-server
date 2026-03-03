package kr.hhplus.be.server.payment.infrastructure;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertDetailRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.application.PaymentCommandService;
import kr.hhplus.be.server.payment.domain.PaymentStatus;
import kr.hhplus.be.server.payment.facade.PaymentFacade;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationQueryService;
import kr.hhplus.be.server.reservation.appication.ReservationTokenService;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.facade.ReservationFacade;
import kr.hhplus.be.server.reservation.infrastructure.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentIntegrationTest {
    @Autowired
    private PaymentFacade paymentFacade;
    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertDetailRepository concertDetailRepository;
    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ReservationCommandService reservationCommandService;
    @Autowired
    private ReservationQueryService reservationQueryService;
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private ReservationTokenService reservationTokenService;

    @Test
    void executePayment_success() {

        // given
        Concert concert = concertRepository.save(Concert.create("아이유 콘서트", ""));
        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.create(detail.getId(), 1)
        );

        Long userId = 100L;

        String token =
                reservationTokenService.issueToken(userId, seat.getId());

        ReservationResponse reservationResponse = reservationFacade.initReservation(seat.getId(), userId, token);

        // when
        PaymentResponse response = paymentFacade.executePayment(reservationResponse.reservationId(), 500L);

        // then
        // 1. 예약 상태 확인
        assertThat(reservationRepository.findById(reservationResponse.reservationId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);

        // 2. 좌석 상태 확인
        assertThat(concertSeatRepository.findById(seat.getId()).orElseThrow().getStatus())
                .isEqualTo(SeatStatus.RESERVED);

        // 3. 결제 생성 확인
        assertThat(response).isNotNull();
        assertThat(response.paymentId()).isNotNull();
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    /* status 변경 로직에 의해(상태 강제 변경 불가) 테스트 제외
    @Test
    void executePayment_expired() {
        // given
        Concert concert = concertRepository.save(Concert.create("아이유 콘서트", ""));
        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.create(detail.getId(), 1)
        );

        Long userId = 100L;

        String token =
                reservationTokenService.issueToken(userId, seat.getId());

        ReservationResponse reservationResponse = reservationFacade.initReservation(seat.getId(), userId, token);

        // 만료처리
        Reservation reservation = reservationRepository.findById(reservationResponse.reservationId()).orElseThrow();
        reservation.setStatusForTest(ReservationStatus.PENDING);
        reservationRepository.saveAndFlush(reservation);
        reservation.expire();
        reservationRepository.saveAndFlush(reservation);

        // when
        PaymentResponse response = paymentFacade.executePayment(reservationResponse.reservationId(), 500L);

        // then
        // 1. 예약 상태 확인
        assertThat(reservationRepository.findById(reservationResponse.reservationId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.EXPIRED);

        // 2. 좌석 상태 확인(예약 -> 이용가능)
        assertThat(concertSeatRepository.findById(seat.getId()).orElseThrow().getStatus())
                .isEqualTo(SeatStatus.AVAILABLE);

        // 3. 결제 실패 생성 확인
        assertThat(response).isNotNull();
        assertThat(response.paymentId()).isNotNull();
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
    }
    */
}