package kr.hhplus.be.server.concert.infrastructure;

import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertDetailRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcertSeatRedisLockIntegrationTest {

    @Autowired
    private ConcertCommandService concertCommandService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDetailRepository concertDetailRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void clearRedis() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .flushAll();
    }

    @PostConstruct
    public void init() {
        concertSeatRepository.deleteAll();
        concertDetailRepository.deleteAll();
        concertRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testChangeSeatStatus_withRedisLock_10Users() throws InterruptedException {
        // 1️⃣ 콘서트/좌석 생성
        Concert concert = concertRepository.save(Concert.create("아이유 콘서트", ""));
        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.create(detail.getId(), 1)
        );

        int totalUsers = 10;
        CountDownLatch latch = new CountDownLatch(totalUsers);
        ExecutorService executor = Executors.newFixedThreadPool(totalUsers);

        Exception[] exceptions = new Exception[totalUsers];
        ConcertSeatStatusResponse[] responses = new ConcertSeatStatusResponse[totalUsers];

        // 2️⃣ 멀티스레드로 동시에 좌석 상태 변경 시도
        for (int i = 0; i < totalUsers; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    responses[idx] = concertCommandService.changeConcertSeatStatus(seat.getId(), SeatStatus.HOLD);
                } catch (Exception e) {
                    exceptions[idx] = e;
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 3️⃣ 검증: 1명 성공, 9명 실패
        int successCount = 0;
        int failCount = 0;
        for (ConcertSeatStatusResponse r : responses) if (r != null) successCount++;
        for (Exception e : exceptions) if (e != null) failCount++;

        assertThat(successCount).isEqualTo(1);
        assertThat(failCount).isEqualTo(9);

        // 좌석 상태 확인
        ConcertSeat updatedSeat = concertSeatRepository.findById(seat.getId()).orElseThrow();
        List<ConcertSeat> totalSeats = concertSeatRepository.findAll();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.HOLD);
        assertThat(totalSeats).hasSize(1);
    }
}