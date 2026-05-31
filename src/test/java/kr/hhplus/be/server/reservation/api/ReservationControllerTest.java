package kr.hhplus.be.server.reservation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.queue.application.QueueService;
import kr.hhplus.be.server.reservation.api.dto.ReservationRequest;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.facade.ReservationFacade;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationFacade reservationFacade;

    @MockitoBean
    private QueueService queueService;

    @Test
    void 예약성공() throws Exception {

        // given
        Long userId = 1L;
        Long concertSeatId = 1L;
        String token = "test-token";

        ReservationRequest request =
                new ReservationRequest(
                        userId,
                        concertSeatId,
                        token
                );

        Reservation reservation =
                Reservation.create(
                        userId,
                        concertSeatId,
                        1L
                );

        ReservationResponse response =
                ReservationResponse.of(reservation);

        Mockito.when(
                queueService.tryAcquire(userId)
        ).thenReturn(true);

        Mockito.when(
                reservationFacade.initReservation(
                        concertSeatId,
                        userId,
                        token
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.reservationId")
                                .value(response.reservationId())
                )
                .andExpect(
                        jsonPath("$.seatStatus")
                                .value(response.seatStatus().name())
                );

        verify(queueService)
                .tryAcquire(userId);

        verify(reservationFacade)
                .initReservation(
                        concertSeatId,
                        userId,
                        token
                );

        verify(queueService)
                .release();
    }
}