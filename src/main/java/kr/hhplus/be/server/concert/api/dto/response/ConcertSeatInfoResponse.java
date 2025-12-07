package kr.hhplus.be.server.concert.api.dto.response;

import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;

public record ConcertSeatInfoResponse(Long concertSeatId,
        Long concertDetailId,
        int seatNo,
        SeatStatus seatStatus) {
    public static ConcertSeatInfoResponse from(ConcertSeat concertSeat) {
        return new ConcertSeatInfoResponse(concertSeat.getId(),
                concertSeat.getConcertDetailId(),
                concertSeat.getSeatNo(),
                concertSeat.getStatus());
    }
}
