package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertSeatRepository extends JpaRepository<ConcertSeat, Long> {
    List<ConcertSeat> findByConcertDetailIdAndStatus(Long concertDetailId, SeatStatus status);
}
