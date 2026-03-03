package kr.hhplus.be.server.concert.application;

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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ConcertQueryService {

    private final ConcertRepository concertRepository;
    private final ConcertDetailRepository concertDetailRepository;
    private final ConcertSeatRepository concertSeatRepository;

    // 모든 콘서트
    @Cacheable(cacheNames = "concerts", key = "'allConcerts'")
    public List<ConcertInfoResponse> getConcerts() {
        return concertRepository.findAll().stream()
                .map(ConcertInfoResponse::from)
                .toList();
    }

    // 콘서트 날짜
    @Cacheable(cacheNames = "concertDates", key = "'concertDates:' + #concertId")
    public ConcertDatesResponse getConcertDates(Long concertId) {
        ConcertInfoResponse concertInfoResponse = concertRepository
                .findById(concertId)
                .map(ConcertInfoResponse::from)
                .orElseThrow(() -> new NotFoundConcertException(concertId));

        List<ConcertDetailInfoResponse> dates = concertDetailRepository.findByConcertId(concertId).stream()
                .map(ConcertDetailInfoResponse::from)
                .toList();

        return new ConcertDatesResponse(concertInfoResponse, dates);
    }

    // 사용 가능한 좌석
    @Cacheable(cacheNames = "availableSeats", key = "'availableSeats:' + #concertDetailId")
    public List<ConcertSeatInfoResponse> getAvailableSeats(Long concertDetailId, LocalDate concertDate) {
        ConcertDetail detail = concertDetailRepository
                .findByIdAndConcertDate(concertDetailId, concertDate)
                .orElseThrow(() -> new NotFoundConcertException(concertDetailId));

        return concertSeatRepository.findByConcertDetailIdAndStatus(detail.getId(), SeatStatus.AVAILABLE).stream()
                .map(ConcertSeatInfoResponse::from)
                .toList();
    }

    // 좌석 캐시 무효화
    @CacheEvict(cacheNames = "availableSeats", key = "'availableSeats:' + #concertDetailId")
    public void evictAvailableSeatsCache(Long concertDetailId) { }
}