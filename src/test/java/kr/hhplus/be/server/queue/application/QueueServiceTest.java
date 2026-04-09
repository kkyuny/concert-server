package kr.hhplus.be.server.queue.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
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
     * ✅ 2. 대기열 상태 확인 테스트
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
    void 마지막_유저는_큐에_대기했다가_진입한다() throws InterruptedException {
        int maxConcurrent = 5;

        CountDownLatch runningLatch = new CountDownLatch(maxConcurrent);
        CountDownLatch doneLatch = new CountDownLatch(maxConcurrent + 1);

        AtomicInteger successCount = new AtomicInteger(0);

        String waitingUser = "user5";

        // 1. 5명 먼저 실행 (슬롯 점유 + 오래 유지)
        for (int i = 0; i < maxConcurrent; i++) {
            final String userId = "user" + i;

            new Thread(() -> {
                boolean permit = queueService.waitForPermit(userId, 5);

                if (permit) {
                    successCount.incrementAndGet();
                    runningLatch.countDown(); // 🔥 실행 진입

                    try {
                        Thread.sleep(2000); // 🔥 슬롯 유지
                    } catch (InterruptedException ignored) {}

                    queueService.releaseSlot();
                }

                doneLatch.countDown();
            }).start();
        }

        // 2. 5명이 실제 실행 상태 될 때까지 대기
        runningLatch.await();

        // 3. 6번째 유저 실행
        Thread t = new Thread(() -> {
            boolean permit = queueService.waitForPermit(waitingUser, 5);
            if (permit) {
                successCount.incrementAndGet();
                queueService.releaseSlot();
            }
            doneLatch.countDown();
        });
        t.start();

        // 🔥 4. 중간 상태에서 "큐 진입" 검증
        long start = System.currentTimeMillis();
        boolean queued = false;

        while (System.currentTimeMillis() - start < 2000) {
            int pos = queueService.getQueuePosition(waitingUser);

            if (pos != -1) { // 🔥 핵심: 큐에 들어갔는지만 본다
                queued = true;
                break;
            }

            Thread.sleep(50);
        }

        assertThat(queued).isTrue(); // ✔ 큐 진입 확인

        // 5. 전체 종료 대기
        doneLatch.await();

        // 6. 최종 검증
        assertThat(successCount.get()).isEqualTo(6);
        assertThat(queueService.getQueuePosition(waitingUser)).isEqualTo(-1);
    }

    @Test
    void 마지막_유저는_슬롯꽉차면_permit_false() throws InterruptedException {
        int totalUsers = 6;
        int maxConcurrent = 5;

        CountDownLatch latch = new CountDownLatch(maxConcurrent); // 앞 5명만 작업
        AtomicInteger successCount = new AtomicInteger(0);

        // 1. 앞 5명 슬롯 점유
        for (int i = 0; i < maxConcurrent; i++) {
            final String userId = "user" + i;
            new Thread(() -> {
                boolean permit = queueService.waitForPermit(userId, 5); // 충분히 기다림
                if (permit) {
                    successCount.incrementAndGet();
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {} // slot 점유 시간
                    queueService.releaseSlot();
                }
                latch.countDown();
            }).start();
        }

        // 2. 5명 스레드가 slot 점유할 시간 확보
        Thread.sleep(100); // CPU 스케줄러에 따라 조금 더 늘려도 됨

        // 3. 마지막 유저 시도
        String lastUser = "user5";
        boolean permitForLast = queueService.waitForPermit(lastUser, 1); // 대기 시간 짧음 → false 예상

        // 4. 앞 5명 종료 대기
        latch.await();

        // 5. 검증
        assertThat(permitForLast).isFalse(); // 마지막 유저는 slot 못 가져야 함
        assertThat(successCount.get()).isEqualTo(maxConcurrent); // 앞 5명만 성공
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