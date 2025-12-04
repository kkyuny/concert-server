package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.infrasturcture.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationCommandServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    
    @InjectMocks
    private ReservationCommandService reservationCommandService;
    
    @Test
    void createPendingReservationTest() {
        Long userId = 1L;
        Long concertSeatId = 1L;

        Reservation reservation = Reservation.create(userId, concertSeatId);

        given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

        ReservationResponse reservationResponse = reservationCommandService.createPendingReservation(userId, concertSeatId);

        assertThat(reservationResponse.reservationId()).isEqualTo(reservation.getId());
    }
}