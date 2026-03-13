package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.NotFoundConcertSeatException;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ConcertCommandService {

    private final ConcertSeatRepository concertSeatRepository;
    private final StringRedisTemplate redisTemplate;

    private static final long LOCK_EXPIRE_SEC = 5; // 락 만료 시간

    @Transactional
    public ConcertSeatStatusResponse changeConcertSeatStatus(Long concertSeatId, SeatStatus seatStatus) {
        String lockKey = "seat:" + concertSeatId + ":lock";
        String lockValue = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(LOCK_EXPIRE_SEC));

        if (Boolean.FALSE.equals(acquired)) {
            throw new IllegalStateException("다른 사용자가 해당 좌석을 먼저 예약하였습니다.");
        }

        try {
            // DB 조회 후 상태 변경
            ConcertSeat concertSeat = concertSeatRepository.findByIdForLock(concertSeatId)
                    .orElseThrow(() -> new NotFoundConcertSeatException(concertSeatId));

            return ConcertSeatStatusResponse.of(concertSeat.changeStatus(seatStatus));
        } finally {
            // 락 해제: 값이 본인이 설정한 값일 때만 삭제
            String currentValue = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}