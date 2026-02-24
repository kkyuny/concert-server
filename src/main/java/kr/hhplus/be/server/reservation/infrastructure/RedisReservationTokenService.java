package kr.hhplus.be.server.reservation.infrastructure;

import kr.hhplus.be.server.reservation.appication.ReservationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisReservationTokenService implements ReservationTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final long TOKEN_TTL = 300; // 5분

    @Override
    public String issueToken(Long userId, Long seatId) {

        String seatKey = "seat:token:" + seatId;

        String token = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(seatKey, token, TOKEN_TTL, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(success)) {
            throw new IllegalStateException("선점되었음.");
        }

        redisTemplate.opsForValue()
                .set("token:" + token,
                        userId + ":" + seatId,
                        TOKEN_TTL,
                        TimeUnit.SECONDS);

        return token;
    }

    @Override
    public void validateToken(String token, Long userId, Long seatId) {

        String value = redisTemplate.opsForValue().get("token:" + token);

        if (value == null) {
            throw new IllegalStateException("토큰 만료");
        }

        String[] split = value.split(":");

        if (!split[0].equals(String.valueOf(userId))
                || !split[1].equals(String.valueOf(seatId))) {
            throw new IllegalStateException("잘못된 토큰");
        }
    }

    @Override
    public void consumeToken(String token, Long seatId) {

        redisTemplate.delete("token:" + token);
        redisTemplate.delete("seat:token:" + seatId);
    }
}