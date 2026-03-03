package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcertSeatConcurrencyTest {

    @Autowired
    private ConcertCommandService concertCommandService;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Test
    void 좌석_동시예약시_하나만_성공() throws InterruptedException {

        // given
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.create(1L, 1)
        );

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();  // 준비 완료
                    startLatch.await();      // 동시에 시작

                    concertCommandService.changeConcertSeatStatus(
                            seat.getId(),
                            SeatStatus.HOLD
                    );

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        readyLatch.await();   // 모든 쓰레드 준비될 때까지 대기
        startLatch.countDown(); // 동시에 시작
        endLatch.await();     // 모두 끝날 때까지 대기

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }
}
