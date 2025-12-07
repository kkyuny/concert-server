package kr.hhplus.be.server.concert.infrastructure;

import kr.hhplus.be.server.concert.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
}
