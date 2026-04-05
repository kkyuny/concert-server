package kr.hhplus.be.server.queue.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QueueServiceTest {

    @Autowired
    QueueService queueService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void flushRedis() {
        assertNotNull(redisTemplate.getConnectionFactory());
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    /**
     * ✅ 1. 최대 동시성 테스트 (MAX_CONCURRENT = 5)
     */
    @Test
    void 동시에_최대_5명만_facade_실행된다() throws InterruptedException {
        int threadCount = 10;

        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger currentRunning = new AtomicInteger(0); // 현재 실행 중
        AtomicInteger maxRunning = new AtomicInteger(0);     // 최대 동시 실행 수

        for (int i = 0; i < threadCount; i++) {
            final String userId = "user" + i;

            new Thread(() -> {
                boolean permit = queueService.waitForPermit(userId, 5);

                if (permit) {
                    int running = currentRunning.incrementAndGet();

                    // 🔥 동시 실행 최대값 기록
                    maxRunning.updateAndGet(prev -> Math.max(prev, running));

                    try {
                        // 👉 facade 실행 시간 가정
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}

                    currentRunning.decrementAndGet();
                    queueService.releaseSlot();
                }

                latch.countDown();
            }).start();
        }

        latch.await();

        // 🔥 핵심 검증
        assertThat(maxRunning.get()).isLessThanOrEqualTo(5);
    }

    /**
     * ✅ 2. 슬롯 반환 후 다음 유저 진입
     */
    @Test
    void 마지막_유저_대기상태_확인() throws InterruptedException {
        int totalUsers = 6;

        // 6명 모두 enqueue만
        for (int i = 0; i < totalUsers; i++) {
            String userId = "user" + i;
            queueService.enqueueIfAbsent("api:reservation:queue", userId);
        }

        List<String> queue = queueService.getQueue("api:reservation:queue");

        // 🔥 큐 길이는 6이어야 함
        assertThat(queue.size()).isEqualTo(totalUsers);

        // 🔥 마지막 유저 인덱스 확인 (대기 상태)
        int lastUserIndex = queue.indexOf("user5"); // 0부터 시작
        assertThat(lastUserIndex).isGreaterThanOrEqualTo(5);
    }

    @Test
    void 마지막_유저가_슬롯_반환_후_진입한다() throws InterruptedException {
        int totalUsers = 6; // 6명

        CountDownLatch latch = new CountDownLatch(totalUsers);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < totalUsers; i++) {
            final String userId = "user" + i;

            new Thread(() -> {
                boolean permit = queueService.waitForPermit(userId, 5);

                if (permit) {
                    successCount.incrementAndGet();
                    try {
                        // facade 실행 시간 가정
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                    queueService.releaseSlot();
                }

                latch.countDown();
            }).start();
        }

        latch.await();

        // 🔥 6번째 유저도 마지막에 진입 가능해야 함
        List<String> queue = queueService.getQueue("api:reservation:queue");
        assertThat(queue).doesNotContain("user5"); // 6번째 유저는 이미 처리되어 큐에서 제거됨

        // 🔥 최대 동시 처리 수는 5
        assertThat(successCount.get()).isEqualTo(6);
    }

    /**
     * ✅ 3. timeout 테스트
     */
    @Test
    void 대기시간_초과시_false_반환() {
        String userId = "u1";

        boolean result = queueService.waitForPermit(userId, 0);

        assertThat(result).isFalse();
    }

    /**
     * ✅ 4. FIFO + 동시성 영역 테스트
     */
    @Test
    void 앞에서_5명만_진입_가능하다() throws InterruptedException {
        int total = 7;
        CountDownLatch latch = new CountDownLatch(total);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < total; i++) {
            final String userId = "u" + i;

            new Thread(() -> {
                boolean r = queueService.waitForPermit(userId, 2);
                if (r) {
                    success.incrementAndGet();
                    // 일부러 release 안 해서 slot 유지
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        // MAX_CONCURRENT = 5
        assertThat(success.get()).isEqualTo(5);
    }
}