package kr.hhplus.be.server.concert.api.dto.response;

import java.util.List;

public record ConcertDatesResponse(ConcertInfoResponse concert, List<ConcertDetailInfoResponse> dates) {
    public static ConcertDatesResponse of(ConcertInfoResponse concert, List<ConcertDetailInfoResponse> dates) {
        return new ConcertDatesResponse(concert, dates);
    }
}
