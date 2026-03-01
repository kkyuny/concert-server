package kr.hhplus.be.server.concert.infrastructure;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConcertSeatRepository extends JpaRepository<ConcertSeat, Long> {
    List<ConcertSeat> findByConcertDetailIdAndStatus(Long concertDetailId, SeatStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ConcertSeat s where s.id = :seatId")
    Optional<ConcertSeat> findByIdForLock(@Param("seatId") Long seatId);
}
