package kr.hhplus.be.server.concert.api.dto.request;

import java.time.LocalDate;

public record ConcertSeatsRequest(Long concertDetailId, LocalDate concertDate) {
}
