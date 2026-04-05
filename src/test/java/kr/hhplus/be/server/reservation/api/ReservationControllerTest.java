package kr.hhplus.be.server.reservation.api;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private ReservationFacade reservationFacade;

    @MockitoBean
    private QueueService queueService;

    @Test
    void reserveConcertTest() throws Exception {
        // given
        Long userId = 1L;
        Long concertSeatId = 1L;
        String token = "test-token";
        ReservationRequest reservationRequest = new ReservationRequest(userId, concertSeatId, token, 1L);
        Reservation reservation = Reservation.create(userId, concertSeatId, 1L);
        ReservationResponse reservationResponse = ReservationResponse.of(reservation);


        Mockito.when(queueService.waitForPermit(anyString(), anyLong()))
                .thenReturn(true);

        when(reservationFacade.initReservation(reservationRequest.concertSeatId(), reservationRequest.userId(), reservationRequest.token(), 1L))
                .thenReturn(reservationResponse);

        // when & then
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Reservation-Token", token)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reservationId").value(reservationResponse.reservationId()))
                .andExpect(jsonPath("$.seatStatus").value(reservationResponse.seatStatus().name()));

        verify(reservationFacade).initReservation(concertSeatId, userId, token, 1L);
    }

}