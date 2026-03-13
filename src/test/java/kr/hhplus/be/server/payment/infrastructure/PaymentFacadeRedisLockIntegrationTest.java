package kr.hhplus.be.server.payment.infrastructure;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertDetailRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.domain.PaymentStatus;
import kr.hhplus.be.server.payment.facade.PaymentFacade;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrastructure.ReservationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentFacadeRedisLockIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDetailRepository concertDetailRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testExecutePayment_10Users_concurrent() throws InterruptedException {
        // 1️⃣ 콘서트/좌석/예약 생성
        Concert concert = concertRepository.save(Concert.create("아이유 콘서트", ""));
        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );
        ConcertSeat seat = concertSeatRepository.save(ConcertSeat.create(detail.getId(), 1));

        Long reservationId = reservationCommandService.createPendingReservation(100L, seat.getId()).reservationId();

        int totalUsers = 10;
        CountDownLatch latch = new CountDownLatch(totalUsers);
        ExecutorService executor = Executors.newFixedThreadPool(totalUsers);

        PaymentResponse[] responses = new PaymentResponse[totalUsers];
        Exception[] exceptions = new Exception[totalUsers];

        // 2️⃣ 멀티스레드로 동시에 결제 시도
        for (int i = 0; i < totalUsers; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    responses[idx] = paymentFacade.executePayment(reservationId, 10000L);
                } catch (Exception e) {
                    exceptions[idx] = e;
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 3️⃣ 검증
        int successCount = 0;
        int failCount = 0;
        for (PaymentResponse r : responses) if (r != null) successCount++;
        for (Exception e : exceptions) if (e != null) failCount++;

        assertThat(successCount).isEqualTo(1);
        assertThat(failCount).isEqualTo(9);

        // 좌석 상태 확인
        assertThat(concertSeatRepository.findById(seat.getId()).orElseThrow().getStatus())
                .isEqualTo(SeatStatus.RESERVED);

        // 예약 상태 확인
        assertThat(reservationRepository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);

        // 결제 성공 여부 확인
        Long payId = 0L;
        for (PaymentResponse r : responses) {
            if (r != null) payId = r.paymentId();
        }
        assertThat(paymentRepository.findById(payId).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.CAPTURED);
    }
}
