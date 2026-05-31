package kr.hhplus.be.server.queue.infrastructure;

import kr.hhplus.be.server.queue.application.QueueService;
import kr.hhplus.be.server.queue.domain.QueueStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueConcurrencyTest {

    @Autowired
    QueueService queueService;

    @Autowired
    RedisQueueRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clear() {
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());
        redisTemplate.getConnectionFactory()
                .getConnection().serverCommands();
    }

    @Test
    void 최대_5명만_ACTIVE_허용() throws Exception {

        int threadCount = 50;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch =
                new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (long i = 1; i <= threadCount; i++) {

            long userId = i;

            executor.submit(() -> {
                try {

                    queueService.enterQueue(userId);

                    boolean acquired =
                            queueService.tryAcquire(userId);

                    if (acquired) {
                        success.incrementAndGet();

                        // active 상태 유지 시뮬레이션
                        Thread.sleep(50);

                        queueService.release();
                    } else {
                        fail.incrementAndGet();
                    }

                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        executor.shutdown();

        // 핵심 검증
        assertThat(success.get()).isEqualTo(5);
        assertThat(fail.get()).isEqualTo(threadCount - 5);
    }

    @Test
    void READY_상태_정상_판별() {

        queueService.enterQueue(1L);
        queueService.enterQueue(2L);
        queueService.enterQueue(3L);
        queueService.enterQueue(4L);
        queueService.enterQueue(5L);
        queueService.enterQueue(6L);

        assertThat(queueService.getStatus(1L))
                .isEqualTo(QueueStatus.READY);

        assertThat(queueService.getStatus(5L))
                .isEqualTo(QueueStatus.READY);

        assertThat(queueService.getStatus(6L))
                .isEqualTo(QueueStatus.WAITING);
    }
}