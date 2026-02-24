package kr.hhplus.be.server.reservation.facade;

import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationTokenService;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {
    @Mock
    private ReservationCommandService reservationCommandService;

    @Mock
    private ConcertCommandService concertCommandService;

    @Mock
    private ReservationTokenService reservationTokenService;

    @InjectMocks
    private ReservationFacade reservationFacade;

    @Test
    void initReservationTest() {
        Long userId = 1L;
        Long concertSeatId = 1L;
        Long concertDetailId = 1L;
        int seatNo = 1;
        String token = "test-token";

        doNothing().when(reservationTokenService)
                .validateToken(token, userId, concertSeatId);

        Reservation reservation = Reservation.create(userId, concertSeatId);
        given(reservationCommandService.createPendingReservation(userId, concertSeatId))
                .willReturn(ReservationResponse.of(reservation));

        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
        given(concertCommandService.changeConcertSeatStaus(concertSeatId, SeatStatus.HOLD))
                .willReturn(ConcertSeatStatusResponse.of(concertSeat));

        ReservationResponse reservationResponse = reservationFacade.initReservation(concertSeatId, userId, token);
        assertThat(reservationResponse.seatStatus()).isEqualTo(ReservationStatus.PENDING);

        verify(reservationTokenService, times(1))
                .validateToken(token, userId, concertSeatId);

        verify(reservationCommandService, times(1))
                .createPendingReservation(userId, concertSeatId);

        verify(concertCommandService, times(1))
                .changeConcertSeatStaus(concertSeatId, SeatStatus.HOLD);
    }
}