package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.domain.ConcertDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConcertDetailRepository extends JpaRepository<ConcertDetail, Long> {
    List<ConcertDetail> findByConcertId(Long concertId);

    Optional<ConcertDetail> findByIdAndConcertDate(Long concertDetailId, LocalDate localDate);
}
