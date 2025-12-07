package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.NotFoundConcertSeatException;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class ConcertCommandService {
    private final ConcertSeatRepository concertSeatRepository;

    public ConcertSeatStatusResponse changeConcertSeatStaus(Long concertSeatId, SeatStatus seatStatus) {
        ConcertSeat concertSeat = concertSeatRepository.findById(concertSeatId)
                .orElseThrow(() -> new NotFoundConcertSeatException(concertSeatId));

        return ConcertSeatStatusResponse.of(concertSeat.changeStatus(seatStatus));
    }
}
