package kr.hhplus.be.server.reservation.facade;

import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {
    @Mock
    private ReservationCommandService reservationCommandService;

    @Mock
    private ConcertCommandService concertCommandService;

    @InjectMocks
    private ReservationFacade reservationFacade;

    @Test
    void initReservationTest() {
        Long userId = 1L;
        Long concertSeatId = 1L;
        Long concertDetailId = 1L;
        int seatNo = 1;

        Reservation reservation = Reservation.create(userId, concertSeatId);
        given(reservationCommandService.createPendingReservation(userId, concertSeatId))
                .willReturn(ReservationResponse.of(reservation));

        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
        given(concertCommandService.changeConcertSeatStaus(concertSeatId, SeatStatus.HOLD))
                .willReturn(ConcertSeatStatusResponse.of(concertSeat));

        ReservationResponse reservationResponse = reservationFacade.initReservation(concertSeatId, userId);
        assertThat(reservationResponse.seatStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}