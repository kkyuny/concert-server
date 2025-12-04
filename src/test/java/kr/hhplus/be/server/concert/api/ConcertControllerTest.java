package kr.hhplus.be.server.concert.api;

import kr.hhplus.be.server.concert.api.dto.request.ConcertSeatsRequest;
import kr.hhplus.be.server.concert.api.dto.response.ConcertDatesResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertDetailInfoResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertInfoResponse;
import kr.hhplus.be.server.concert.api.dto.response.ConcertSeatInfoResponse;
import kr.hhplus.be.server.concert.application.ConcertQueryService;
import kr.hhplus.be.server.concert.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConcertController.class)
class ConcertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConcertQueryService concertQueryService;

    @Test
    void getConcerts() throws Exception {
        // given
        Concert c1 = ConcertFixture.createConcert(1L);
        Concert c2 = ConcertFixture.createConcert(2L);
        Concert c3 = ConcertFixture.createConcert(3L);

        List<ConcertInfoResponse> data = List.of(
                ConcertInfoResponse.from(c1),
                ConcertInfoResponse.from(c2),
                ConcertInfoResponse.from(c3)
        );

        when(concertQueryService.getConcerts()).thenReturn(data);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/concerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].concertId").value(1))
                .andExpect(jsonPath("$[1].concertId").value(2))
                .andExpect(jsonPath("$[2].concertId").value(3));
    }

    @Test
    void getConcertDates() throws Exception {
        Concert c = ConcertFixture.createConcert(1L);
        ConcertDetail cd1 = ConcertDetailFixture.createConcertDetail(c, 1L, LocalDate.of(2025,11,25), 100);
        ConcertDetail cd2 = ConcertDetailFixture.createConcertDetail(c, 2L, LocalDate.of(2025,11,26), 100);
        ConcertDetail cd3 = ConcertDetailFixture.createConcertDetail(c, 3L, LocalDate.of(2025,11,27), 100);

        List<ConcertDetailInfoResponse> cdir = Stream.of(cd1, cd2, cd3).map(ConcertDetailInfoResponse::from).toList();

        ConcertDatesResponse cdr = ConcertDatesResponse.of(ConcertInfoResponse.from(c), cdir);

        when(concertQueryService.getConcertDates(any())).thenReturn(cdr);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/concerts/dates")
                        .param("concertId", "1"))
                .andExpect(status().isOk())
                // 콘서트 정보
                .andExpect(jsonPath("$.concert.concertId").value(1))
                // 날짜 리스트 길이
                .andExpect(jsonPath("$.dates.length()").value(3))
                // 각 날짜 검증
                .andExpect(jsonPath("$.dates[0].concertDetailId").value(1))
                .andExpect(jsonPath("$.dates[0].concertDate").value("2025-11-25"))
                .andExpect(jsonPath("$.dates[1].concertDetailId").value(2))
                .andExpect(jsonPath("$.dates[1].concertDate").value("2025-11-26"))
                .andExpect(jsonPath("$.dates[2].concertDetailId").value(3))
                .andExpect(jsonPath("$.dates[2].concertDate").value("2025-11-27"));
    }
    
    @Test
    void getAvailableSeatsTest() throws Exception {
        ConcertDetail cd = ConcertDetailFixture.createConcertDetail(LocalDate.of(2025,11,25), 100);
        List<ConcertSeat> css = ConcertSeatFixture.createConcertSeats(cd.getId(), 10);
        ConcertSeatsRequest csr = new ConcertSeatsRequest(cd.getId(), cd.getConcertDate());
        List<ConcertSeatInfoResponse> csir = css.stream().map(ConcertSeatInfoResponse::from).toList();

        when(concertQueryService.getAvailableSeats(csr.concertDetailId(), csr.concertDate())).thenReturn(csir);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/concerts/seats")
                .param("concertDetailId", "1")
                .param("concertDate", "2025-11-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].seatNo").exists())
                .andExpect(jsonPath("$[0].seatStatus").exists())
                .andExpect(jsonPath("$[0].concertDetailId").value(cd.getId()));;

    }
}
