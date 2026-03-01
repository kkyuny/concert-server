package kr.hhplus.be.server.reservation.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RedisReservationTokenServiceTest {

    @Autowired
    private RedisReservationTokenService tokenService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());
        redisTemplate.getConnectionFactory()
                .getConnection()
                .flushAll(); // 테스트 후 Redis 초기화
    }

    @Test
    void issueToken_success() {

        Long userId = 1L;
        Long seatId = 100L;

        String token = tokenService.issueToken(userId, seatId);

        assertThat(token).isNotNull();

        String seatValue =
                redisTemplate.opsForValue().get("seat:token:" + seatId);

        assertThat(seatValue).isEqualTo(token);

        String tokenValue =
                redisTemplate.opsForValue().get("token:" + token);

        assertThat(tokenValue).isEqualTo(userId + ":" + seatId);
    }

    @Test
    void issueToken_fail_whenSeatAlreadyTaken() {

        Long userId1 = 1L;
        Long userId2 = 2L;
        Long seatId = 100L;

        tokenService.issueToken(userId1, seatId);

        assertThatThrownBy(() ->
                tokenService.issueToken(userId2, seatId)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void validateToken_success() {

        Long userId = 1L;
        Long seatId = 100L;

        String token = tokenService.issueToken(userId, seatId);

        assertThatCode(() ->
                tokenService.validateToken(token, userId, seatId)
        ).doesNotThrowAnyException();
    }

    @Test
    void consumeToken_success() {

        Long userId = 1L;
        Long seatId = 100L;

        String token = tokenService.issueToken(userId, seatId);

        tokenService.consumeToken(token, seatId);

        assertThat(redisTemplate.hasKey("token:" + token)).isFalse();
        assertThat(redisTemplate.hasKey("seat:token:" + seatId)).isFalse();
    }

    @Test
    void concurrency_test() throws InterruptedException {

        int threadCount = 10;
        ExecutorService executorService =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch = new CountDownLatch(threadCount);

        Long seatId = 100L;

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {

            final Long userId = (long) i;

            executorService.submit(() -> {
                try {
                    tokenService.issueToken(userId, seatId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }
}