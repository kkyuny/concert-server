package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ConcertSeatIntegrationTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDetailRepository concertDetailRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Test
    void findByConcertDetailIdAndStatus() {

        // given
        Concert concert = concertRepository.save(
                Concert.create("아이유 콘서트", "")
        );

        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );

        concertSeatRepository.save(
                ConcertSeat.create(detail.getId(), 1)
        );

        concertSeatRepository.save(
                ConcertSeat.create(detail.getId(), 2)
        );

        // when
        List<ConcertSeat> result =
                concertSeatRepository.findByConcertDetailIdAndStatus(
                        detail.getId(),
                        SeatStatus.AVAILABLE
                );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getSeatNo()).isEqualTo(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }
}