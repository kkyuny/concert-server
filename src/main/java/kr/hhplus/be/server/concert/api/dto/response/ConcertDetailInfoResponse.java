package kr.hhplus.be.server.concert.api.dto.response;

import kr.hhplus.be.server.concert.domain.ConcertDetail;

import java.time.LocalDate;

public record ConcertDetailInfoResponse(
        Long concertDetailId,
        Long concertId,
        int price,
        LocalDate concertDate
) {
    public static ConcertDetailInfoResponse from(ConcertDetail concertDetail) {
        return new ConcertDetailInfoResponse(concertDetail.getId(),
                concertDetail.getConcert().getId(),
                concertDetail.getPrice(),
                concertDetail.getConcertDate());
    }
}
