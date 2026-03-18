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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void clearRedis() {
        Assertions.assertNotNull(stringRedisTemplate.getConnectionFactory());
        stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .flushAll();
    }

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

        ReservationResponse reservationResponse = reservationFacade.initReservation(seat.getId(), userId, token, concert.getId());

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

    @Test
    void executePayment_ranking_집계_정상동작() {
        // given
        Concert concert1 = concertRepository.save(Concert.create("콘서트1", ""));
        Concert concert2 = concertRepository.save(Concert.create("콘서트2", ""));

        ConcertDetail detail1 = concertDetailRepository.save(
                ConcertDetail.create(concert1, LocalDate.now(), 500)
        );
        ConcertDetail detail2 = concertDetailRepository.save(
                ConcertDetail.create(concert2, LocalDate.now(), 500)
        );

        ConcertSeat seat1 = concertSeatRepository.save(ConcertSeat.create(detail1.getId(), 1));
        ConcertSeat seat2 = concertSeatRepository.save(ConcertSeat.create(detail1.getId(), 2));
        ConcertSeat seat3 = concertSeatRepository.save(ConcertSeat.create(detail1.getId(), 3));
        ConcertSeat seat4 = concertSeatRepository.save(ConcertSeat.create(detail1.getId(), 4));

        ConcertSeat seat5 = concertSeatRepository.save(ConcertSeat.create(detail2.getId(), 1));
        ConcertSeat seat6 = concertSeatRepository.save(ConcertSeat.create(detail2.getId(), 2));

        Long userId = 100L;

        // concert1 → 4번 결제
        for (ConcertSeat seat : List.of(seat1, seat2, seat3, seat4)) {
            String token = reservationTokenService.issueToken(userId, seat.getId());
            ReservationResponse res = reservationFacade.initReservation(seat.getId(), userId, token, concert1.getId());
            paymentFacade.executePayment(res.reservationId(), 500L);
        }

        // concert2 → 2번 결제
        for (ConcertSeat seat : List.of(seat5, seat6)) {
            String token = reservationTokenService.issueToken(userId, seat.getId());
            ReservationResponse res = reservationFacade.initReservation(seat.getId(), userId, token, concert2.getId());
            paymentFacade.executePayment(res.reservationId(), 500L);
        }

        // when
        String dailyKey = "concert:ranking:daily:" + LocalDate.now();

        Double score1 = stringRedisTemplate.opsForZSet()
                .score(dailyKey, concert1.getId().toString());

        Double score2 = stringRedisTemplate.opsForZSet()
                .score(dailyKey, concert2.getId().toString());

        // then
        assertThat(score1).isEqualTo(4.0);
        assertThat(score2).isEqualTo(2.0);
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