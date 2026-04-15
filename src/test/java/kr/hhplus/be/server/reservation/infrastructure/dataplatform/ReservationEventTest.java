package kr.hhplus.be.server.reservation.infrastructure.dataplatform;

import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import kr.hhplus.be.server.reservation.appication.ReservationTokenService;
import kr.hhplus.be.server.reservation.facade.ReservationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class ReservationEventTest {

    @Autowired
    ReservationFacade reservationFacade;

    @Autowired
    ConcertSeatRepository concertSeatRepository;

    @MockitoBean
    DataPlatformClient dataPlatformClient;

    @MockitoBean
    ReservationTokenService reservationTokenService;

    private Long seatId;

    @BeforeEach
    void setUp() {
        ConcertSeat seat = ConcertSeat.create(1L, 1);
        concertSeatRepository.save(seat);

        seatId = seat.getId();
    }

    @Test
    void 예약_성공시_이벤트_발행된다() {

        doNothing()
                .when(reservationTokenService)
                .validateToken(anyString(), anyLong(), anyLong());

        reservationFacade.initReservation(
                seatId,
                1L,
                "token",
                100L
        );

        verify(dataPlatformClient, times(1))
                .send(anyLong(), anyLong(), anyLong(), anyLong());
    }
}