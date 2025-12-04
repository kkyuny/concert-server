package kr.hhplus.be.server.concert.api;


import kr.hhplus.be.server.concert.api.dto.request.ConcertSeatsRequest;
import kr.hhplus.be.server.concert.api.dto.response.ConcertDatesResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertInfoResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertSeatInfoResponse;
import kr.hhplus.be.server.concert.application.ConcertQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    private final ConcertQueryService concertQueryService;

    @GetMapping
    public List<ConcertInfoResponse> getConcerts() {
        return concertQueryService.getConcerts();
    }

    @GetMapping("/dates")
    public ConcertDatesResponse getConcertDates(Long concertId) {
        return concertQueryService.getConcertDates(concertId);
    }

    @GetMapping("/seats")
    public List<ConcertSeatInfoResponse> getAvailableSeats(ConcertSeatsRequest request) {
        return concertQueryService.getAvailableSeats(request.concertDetailId(), request.concertDate());
    }
}
