package kr.hhplus.be.server.queue.application;

import kr.hhplus.be.server.TestKafkaConfiguration;
import kr.hhplus.be.server.TestRedisConfiguration;
import kr.hhplus.be.server.queue.domain.QueueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({
        TestRedisConfiguration.class,
        TestKafkaConfiguration.class
})
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("reservation:queue");
        redisTemplate.delete("reservation:users");
        redisTemplate.delete("reservation:active");
    }

    @Test
    void 최초_5명은_READY() {

        // given
        for (long i = 1; i <= 5; i++) {
            queueService.enterQueue(i);
        }

        // then
        for (long i = 1; i <= 5; i++) {
            assertThat(queueService.getStatus(i))
                    .isEqualTo(QueueStatus.READY);
        }
    }

    @Test
    void 여섯번째부터는_WAITING() {

        // given
        for (long i = 1; i <= 10; i++) {
            queueService.enterQueue(i);
        }

        // then
        assertThat(queueService.getStatus(6L))
                .isEqualTo(QueueStatus.WAITING);

        assertThat(queueService.getStatus(10L))
                .isEqualTo(QueueStatus.WAITING);
    }

    @Test
    void READY_사용자는_슬롯획득성공() {

        // given
        queueService.enterQueue(1L);

        // when
        boolean result =
                queueService.tryAcquire(1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void WAITING_사용자는_슬롯획득실패() {

        // given
        for (long i = 1; i <= 6; i++) {
            queueService.enterQueue(i);
        }

        // when
        boolean result =
                queueService.tryAcquire(6L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 슬롯획득시_큐에서_제거된다() {

        // given
        queueService.enterQueue(1L);

        // when
        queueService.tryAcquire(1L);

        // then
        Long size =
                redisTemplate.opsForList()
                        .size("reservation:queue");

        assertThat(size).isZero();
    }

    @Test
    void 슬롯반납시_active가_감소한다() {

        // given
        queueService.enterQueue(1L);
        queueService.tryAcquire(1L);

        // when
        queueService.release();

        // then
        String active =
                redisTemplate.opsForValue()
                        .get("reservation:active");

        assertThat(active)
                .isEqualTo("0");
    }

    @Test
    void 같은유저는_중복진입되지않는다() {

        // given
        queueService.enterQueue(1L);
        queueService.enterQueue(1L);
        queueService.enterQueue(1L);

        // when
        Long size =
                redisTemplate.opsForList()
                        .size("reservation:queue");

        // then
        assertThat(size)
                .isEqualTo(1);
    }

    @Test
    void 동시에_50명_진입해도_READY는_5명() throws Exception {

        // given
        int threadCount = 50;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch =
                new CountDownLatch(threadCount);

        for (long i = 1; i <= threadCount; i++) {

            long userId = i;

            executor.submit(() -> {
                try {
                    queueService.enterQueue(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        // when
        latch.await();

        long readyCount =
                LongStream.rangeClosed(1, 50)
                        .filter(id ->
                                queueService.getStatus(id)
                                        == QueueStatus.READY
                        )
                        .count();

        // then
        assertThat(readyCount)
                .isEqualTo(5);
    }
}