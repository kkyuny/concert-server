package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.api.dto.request.ConcertSeatsRequest;
import kr.hhplus.be.server.concert.api.dto.response.ConcertDatesResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertInfoResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertSeatInfoResponse;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertDetailRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConcertQueryServiceTest {
    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertDetailRepository concertDetailRepository;

    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @InjectMocks
    private ConcertQueryService concertQueryService;

    @Test
    void getConcertsTest() {
        // given
        Concert concert = Concert.create("title", "desc");
        ReflectionTestUtils.setField(concert, "id", 1L);

        given(concertRepository.findAll())
                .willReturn(List.of(concert));

        // when
        List<ConcertInfoResponse> responses = concertQueryService.getConcerts();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().concertId()).isEqualTo(1L);
        assertThat(responses.getFirst().title()).isEqualTo("title");
        assertThat(responses.getFirst().description()).isEqualTo("desc");
    }

    @Test
    void getConcertDatesTest() {
        // given
        Long concertId = 1L;
        Long concertDetailId = 10L;

        Concert concert = Concert.create("title", "desc");
        ReflectionTestUtils.setField(concert, "id", concertId);

        ConcertDetail detail = ConcertDetail.create(concert, LocalDate.of(2025, 11, 24), 500);
        ReflectionTestUtils.setField(detail, "id", concertDetailId);

        // mock 설정
        given(concertRepository.findById(concertId))
                .willReturn(Optional.of(concert));

        given(concertDetailRepository.findByConcertId(concertId))
                .willReturn(List.of(detail));

        // when
        ConcertDatesResponse response = concertQueryService.getConcertDates(concertId);

        // then
        assertThat(response.concert().concertId()).isEqualTo(1L);
        assertThat(response.dates()).hasSize(1);
        assertThat(response.dates().getFirst().concertDetailId()).isEqualTo(10L);
        assertThat(response.dates().getFirst().concertDate()).isEqualTo(LocalDate.of(2025, 11, 24));
        assertThat(response.dates().getFirst().price()).isEqualTo(500);
    }

    @Test
    void getAvailableSeatsTest() {
        // given
        Long concertId = 1L;
        Long concertDetailId = 10L;

        ConcertSeatsRequest request = new ConcertSeatsRequest(
                concertDetailId,
                LocalDate.of(2025, 1, 1)
        );

        Concert concert = Concert.create("title", "desc");
        ReflectionTestUtils.setField(concert, "id", concertId);

        ConcertDetail detail = ConcertDetail.create(concert, LocalDate.of(2025, 11, 24), 500);
        ReflectionTestUtils.setField(detail, "id", concertDetailId);

        ConcertSeat seat1 = ConcertSeat.create(concertDetailId, 5);
        ReflectionTestUtils.setField(seat1, "id", 100L);

        // mock 설정
        given(concertDetailRepository.findByIdAndConcertDate(
                concertDetailId,
                LocalDate.of(2025, 1, 1)
        )).willReturn(Optional.of(detail));

        given(concertSeatRepository.findByConcertDetailIdAndStatus(
                concertDetailId,
                SeatStatus.AVAILABLE
        )).willReturn(List.of(seat1));

        // when
        List<ConcertSeatInfoResponse> responses =
                concertQueryService.getAvailableSeats(request.concertDetailId(), request.concertDate());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().concertSeatId()).isEqualTo(100L);
        assertThat(responses.getFirst().seatStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void getAvailableSeats_NoAvailableSeats_ReturnsEmptyList() {
        // given
        Long concertId = 1L;
        Long concertDetailId = 10L;
        LocalDate date = LocalDate.of(2025, 1, 1);

        ConcertSeatsRequest request = new ConcertSeatsRequest(concertDetailId, date);

        Concert concert = Concert.create("title", "desc");
        ReflectionTestUtils.setField(concert, "id", concertId);

        ConcertDetail detail = ConcertDetail.create(concert, date, 500);
        ReflectionTestUtils.setField(detail, "id", concertDetailId);

        given(concertDetailRepository.findByIdAndConcertDate(concertDetailId, date))
                .willReturn(Optional.of(detail));

        // AVAILABLE 좌석 없음
        given(concertSeatRepository.findByConcertDetailIdAndStatus(concertDetailId, SeatStatus.AVAILABLE))
                .willReturn(List.of());

        // when
        List<ConcertSeatInfoResponse> responses = concertQueryService.getAvailableSeats(request.concertDetailId(), request.concertDate());

        // then
        assertThat(responses).isEmpty();
    }

}