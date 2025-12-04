package kr.hhplus.be.server.concert.api.dto;

import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;

public record ConcertSeatStatusResponse(Long concertSeatId, SeatStatus seatStatus) {
    public static ConcertSeatStatusResponse of(ConcertSeat concertSeat) {
        return new ConcertSeatStatusResponse(concertSeat.getId(), concertSeat.getStatus());
    }
}
