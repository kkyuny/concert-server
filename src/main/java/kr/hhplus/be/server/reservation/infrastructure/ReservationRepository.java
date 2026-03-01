package kr.hhplus.be.server.reservation.infrastructure;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :reservationId")
    Optional<Reservation> findByIdWithLock(@Param("reservationId") Long reservationId);

    @Query("""
        select r
        from Reservation r
        where r.status = kr.hhplus.be.server.reservation.domain.ReservationStatus.PENDING
        and r.expiredAt <= :now
    """)
    List<Reservation> findExpiredPending(@Param("now") LocalDateTime now);
}
