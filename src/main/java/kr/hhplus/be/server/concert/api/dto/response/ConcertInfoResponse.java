package kr.hhplus.be.server.concert.api.dto.response;

import kr.hhplus.be.server.concert.domain.Concert;

public record ConcertInfoResponse(
        Long concertId,
        String title,
        String description
) {
    public static ConcertInfoResponse from(Concert concert) {
        return new ConcertInfoResponse(concert.getId(),
                concert.getTitle(),
                concert.getDescription());
    }
}
