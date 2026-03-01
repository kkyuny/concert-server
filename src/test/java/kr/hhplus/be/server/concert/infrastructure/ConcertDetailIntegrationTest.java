package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ConcertDetailIntegrationTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDetailRepository concertDetailRepository;

    @Test
    void findByConcertId() {
        // given
        Concert concert = concertRepository.save(
                Concert.create("아이유 콘서트", "")
        );

        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );

        // when
        List<ConcertDetail> result =
                concertDetailRepository.findByConcertId(concert.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getConcertDate())
                .isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.getFirst().getPrice())
                .isEqualTo(500);
    }

    @Test
    void findByIdAndConcertDate_notFound() {
        assertThat(concertDetailRepository
                .findByIdAndConcertDate(1L, LocalDate.now())
        ).isEmpty();
    }
}