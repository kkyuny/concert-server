package kr.hhplus.be.server.queue.infrastructure;

import kr.hhplus.be.server.queue.application.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueConcurrencyTest {

    @Autowired
    QueueService queueService;

    @Autowired
    RedisQueueRepository repository;

    @BeforeEach
    void clear() {

        repository.clear();
    }

    @Test
    void 최대_5명만_활성화() throws Exception {

        int threadCount = 50;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch =
                new CountDownLatch(threadCount);

        AtomicInteger success =
                new AtomicInteger();

        for (long i = 1; i <= threadCount; i++) {

            long userId = i;

            executor.submit(() -> {

                try {

                    queueService.enter(userId);

                    boolean permit =
                            queueService.waitForPermit(userId);

                    if (permit) {

                        success.incrementAndGet();

                        Thread.sleep(1000);

                        queueService.complete(userId);
                    }

                } catch (Exception ignored) {

                } finally {

                    latch.countDown();
                }
            });
        }

        latch.await();

        assertThat(success.get())
                .isGreaterThan(0);
    }
}
