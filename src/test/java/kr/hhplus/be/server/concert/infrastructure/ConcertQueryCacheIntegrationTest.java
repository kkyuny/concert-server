package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.application.ConcertQueryService;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcertQueryCacheIntegrationTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDetailRepository concertDetailRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ConcertQueryService concertQueryService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Long concertId;
    private Long concertDetailId;

    @BeforeEach
    void setup() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        Concert concert = concertRepository.save(Concert.create("아이유 콘서트", ""));
        concertId = concert.getId();

        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );
        concertDetailId = detail.getId();

        concertSeatRepository.save(ConcertSeat.create(concertDetailId, 1));
    }

    @Test
    void testConcertsCache() {
        concertQueryService.getConcerts();

        Set<String> keys = redisTemplate.keys("*");
        System.out.println("Redis keys: " + keys);

        assertThat(keys).contains("concerts::allConcerts");
    }

    @Test
    void testConcertDatesCache() {
        concertQueryService.getConcertDates(concertId);

        Set<String> keys = redisTemplate.keys("*");
        System.out.println("Redis keys: " + keys);

        assertThat(keys).contains("concertDates::concertDates:" + concertId);
    }

    @Test
    void testAvailableSeatsCache() {
        LocalDate date = LocalDate.of(2026, 3, 1);
        concertQueryService.getAvailableSeats(concertDetailId, date);

        Set<String> keys = redisTemplate.keys("*");
        System.out.println("Redis keys: " + keys);

        assertThat(keys).contains("availableSeats::availableSeats:" + concertDetailId);

        // 캐시 무효화 후 다시 조회
        concertQueryService.evictAvailableSeatsCache(concertDetailId);
        concertQueryService.getAvailableSeats(concertDetailId, date);

        keys = redisTemplate.keys("*");
        System.out.println("Redis keys after eviction: " + keys);
        assertThat(keys).contains("availableSeats::availableSeats:" + concertDetailId);
    }

    @Test
    void printAllCacheKeys() {
        Set<String> keys = redisTemplate.keys("*");
        System.out.println("All Redis keys: " + keys);
    }
}