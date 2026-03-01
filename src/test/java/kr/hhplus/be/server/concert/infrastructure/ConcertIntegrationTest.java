package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.domain.Concert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ConcertIntegrationTest {
    @Autowired
    ConcertRepository concertRepository;

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