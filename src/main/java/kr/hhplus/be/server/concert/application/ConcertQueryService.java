package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.api.dto.request.ConcertSeatsRequest;
import kr.hhplus.be.server.concert.api.dto.response.ConcertDatesResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertDetailInfoResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertInfoResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertSeatInfoResponse;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.NotFoundConcertException;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertDetailRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ConcertQueryService {

    private final ConcertRepository concertRepository;
    private final ConcertDetailRepository concertDetailRepository;
    private final ConcertSeatRepository concertSeatRepository;

    public List<ConcertInfoResponse> getConcerts() {
        return concertRepository.findAll().stream().map(ConcertInfoResponse::from).toList();
    }

    public ConcertDatesResponse getConcertDates(Long concertId) {
        ConcertInfoResponse concertInfoResponse = concertRepository
                .findById(concertId)
                .map(ConcertInfoResponse::from)
                .orElseThrow(() -> new NotFoundConcertException(concertId));

        List<ConcertDetailInfoResponse> dates = concertDetailRepository.findByConcertId(concertId).stream()
                .map(ConcertDetailInfoResponse::from).toList();

        return new ConcertDatesResponse(concertInfoResponse, dates);
    }

    public List<ConcertSeatInfoResponse> getAvailableSeats(Long concertDetailId, LocalDate concertDate) {
        ConcertDetail detail = concertDetailRepository
                .findByIdAndConcertDate(concertDetailId, concertDate)
                .orElseThrow(() -> new NotFoundConcertException(concertDetailId));

        return concertSeatRepository.findByConcertDetailIdAndStatus(detail.getId(), SeatStatus.AVAILABLE).stream()
                .map(ConcertSeatInfoResponse::from).toList();
    }
}
