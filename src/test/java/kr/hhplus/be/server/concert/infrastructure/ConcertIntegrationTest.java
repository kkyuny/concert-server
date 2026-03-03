package kr.hhplus.be.server.concert.infrastructure;

import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.concert.domain.Concert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ConcertIntegrationTest {
    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ConcertDetailRepository concertDetailRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void init() {
        concertSeatRepository.deleteAll();
        concertDetailRepository.deleteAll();
        concertRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void createConcertTest(){
        // given
        Concert concert = Concert.create("test", "");
        concertRepository.save(concert);

        // when
        List<Concert> concertList = concertRepository.findAll();

        //then
        assertThat(concertList).isNotEmpty();
        assertThat(concertList.getFirst().getTitle()).isEqualTo("test");
    }
}