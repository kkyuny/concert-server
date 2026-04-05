package kr.hhplus.be.server.queue.application;

import kr.hhplus.be.server.queue.infrastructure.RedisQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class QueueService {

    private static final int MAX_CONCURRENT = 5;
    private static final long SLOT_TTL = 30; // 초
    private static final String SLOT_KEY = "api:reservation:slot";
    private static final String QUEUE_KEY = "api:reservation:queue";

    private final RedisQueueRepository redisQueueRepository;

    public boolean waitForPermit(String userId, long maxWaitSeconds) {
        redisQueueRepository.enqueueIfAbsent(QUEUE_KEY, userId);

        Instant start = Instant.now();

        while (Duration.between(start, Instant.now()).getSeconds() < maxWaitSeconds) {

            List<String> queue = redisQueueRepository.getQueue(QUEUE_KEY);
            if (queue == null || queue.isEmpty()) {
                sleep(50);
                continue;
            }

            int myIndex = queue.indexOf(userId);

            // 🔥 핵심: 앞에서 MAX_CONCURRENT명만 허용
            if (myIndex == -1 || myIndex >= MAX_CONCURRENT) {
                sleep(100);
                continue;
            }

            boolean acquired = redisQueueRepository.tryAcquireSlot(
                    SLOT_KEY, MAX_CONCURRENT, SLOT_TTL
            );

            if (acquired) {
                redisQueueRepository.removeFromQueue(QUEUE_KEY, userId);
                return true;
            }

            sleep(100);
        }

        // timeout 시 제거
        redisQueueRepository.removeFromQueue(QUEUE_KEY, userId);
        return false;
    }

    public void releaseSlot() {
        redisQueueRepository.releaseSlot(SLOT_KEY);
    }

    public int getQueuePosition(String userId) {
        var list = redisQueueRepository.getQueue(QUEUE_KEY);
        if (list == null) return -1;
        return list.indexOf(userId);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    public List<String> getQueue(String key) {
        return redisQueueRepository.getQueue(key);
    }

    public void enqueueIfAbsent(String key, String userId) {
        redisQueueRepository.enqueueIfAbsent(key, userId);
    }
}