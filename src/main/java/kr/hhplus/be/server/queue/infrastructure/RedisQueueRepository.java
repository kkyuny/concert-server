package kr.hhplus.be.server.queue.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Repository
public class RedisQueueRepository {

    private final RedisTemplate<String, String> redisTemplate;

    // ----------------------------
    // 슬롯 (세마포어)
    // ----------------------------

    public boolean tryAcquireSlot(String slotKey, int maxConcurrent, long ttlSeconds) {
        Long current = redisTemplate.opsForValue().increment(slotKey);
        if (current == null) return false;

        // 🔥 초과 시 롤백
        if (current > maxConcurrent) {
            redisTemplate.opsForValue().decrement(slotKey);
            return false;
        }

        // 🔥 TTL은 최초 생성 시에만 설정 (덮어쓰기 방지)
        Long ttl = redisTemplate.getExpire(slotKey, TimeUnit.SECONDS);
        if (ttl == null || ttl == -1) {
            redisTemplate.expire(slotKey, ttlSeconds, TimeUnit.SECONDS);
        }

        return true;
    }

    public void releaseSlot(String slotKey) {
        Long current = redisTemplate.opsForValue().decrement(slotKey);

        // 🔥 음수 방지
        if (current != null && current < 0) {
            redisTemplate.opsForValue().set(slotKey, "0");
        }
    }

    public Long getSlotTTL(String slotKey) {
        return redisTemplate.getExpire(slotKey, TimeUnit.SECONDS);
    }

    // ----------------------------
    // 큐 (FIFO)
    // ----------------------------

    /**
     * 중복 방지 enqueue
     */
    public void enqueueIfAbsent(String queueKey, String userId) {
        String setKey = queueKey + ":set";

        Long added = redisTemplate.opsForSet().add(setKey, userId);

        if (added != null && added == 1) {
            redisTemplate.opsForList().rightPush(queueKey, userId);
        }
    }

    /**
     * 큐 맨 앞 조회 (디버깅용 / 일부 로직용)
     */
    public String peek(String queueKey) {
        return redisTemplate.opsForList().index(queueKey, 0);
    }

    /**
     * 큐에서 제거 (앞에서 pop)
     */
    public void dequeue(String queueKey) {
        String userId = redisTemplate.opsForList().leftPop(queueKey);

        if (userId != null) {
            redisTemplate.opsForSet().remove(queueKey + ":set", userId);
        }
    }

    /**
     * 특정 유저 제거 (timeout / 성공 시 사용)
     */
    public void removeFromQueue(String queueKey, String userId) {
        Long removed = redisTemplate.opsForList().remove(queueKey, 0, userId);

        // 🔥 실제로 제거된 경우만 SET에서도 제거
        if (removed != null && removed > 0) {
            redisTemplate.opsForSet().remove(queueKey + ":set", userId);
        }
    }

    /**
     * 전체 큐 조회 (테스트/디버깅용)
     */
    public List<String> getQueue(String queueKey) {
        return redisTemplate.opsForList().range(queueKey, 0, -1);
    }

    /**
     * 현재 슬롯 사용량 조회 (테스트용)
     */
    public Long getCurrentSlotCount(String slotKey) {
        String value = redisTemplate.opsForValue().get(slotKey);
        return value == null ? 0L : Long.valueOf(value);
    }
}