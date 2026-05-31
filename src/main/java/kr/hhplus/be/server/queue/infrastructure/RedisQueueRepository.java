package kr.hhplus.be.server.queue.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisQueueRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean addUser(String userId) {

        Long added =
                redisTemplate.opsForSet()
                        .add("reservation:users", userId);

        if (added != null && added > 0) {
            redisTemplate.opsForList()
                    .rightPush("reservation:queue", userId);

            return true;
        }

        return false;
    }

    public Long queueSize() {

        Long size =
                redisTemplate.opsForList()
                        .size("reservation:queue");

        return size == null ? 0 : size;
    }

    public int activeCount() {

        String value =
                redisTemplate.opsForValue()
                        .get("reservation:active");

        return value == null
                ? 0
                : Integer.parseInt(value);
    }

    public void incrementActive() {
        redisTemplate.opsForValue()
                .increment("reservation:active");
    }

    public void decrementActive() {
        redisTemplate.opsForValue()
                .decrement("reservation:active");
    }

    public void removeUser(String userId) {

        redisTemplate.opsForList()
                .remove("reservation:queue", 1, userId);

        redisTemplate.opsForSet()
                .remove("reservation:users", userId);
    }

    public List<String> queue() {

        return redisTemplate.opsForList()
                .range("reservation:queue", 0, -1);
    }
}