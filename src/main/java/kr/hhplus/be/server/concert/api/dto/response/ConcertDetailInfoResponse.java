package kr.hhplus.be.server.concert.api.dto.response;

import kr.hhplus.be.server.concert.domain.ConcertDetail;

import java.time.LocalDate;

public record ConcertDetailInfoResponse(
        Long concertDetailId,
        int price,
        LocalDate concertDate
) {
    public static ConcertDetailInfoResponse from(ConcertDetail concertDetail) {
        return new ConcertDetailInfoResponse(concertDetail.getId(),
                concertDetail.getPrice(),
                concertDetail.getConcertDate());
    }
}
