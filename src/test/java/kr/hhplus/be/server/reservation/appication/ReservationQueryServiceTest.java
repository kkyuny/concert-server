package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.reservation.api.dto.ReservationInfoResponse;
import kr.hhplus.be.server.reservation.domain.NotFoundReservationException;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.infrasturcture.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationQueryServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationQueryService reservationQueryService;

    @Test
    void getReservation() {
        Long userId = 1L;
        Long concertSeatId = 1L;
        Long reservationId = 1L;

        Reservation reservation = Reservation.create(userId, concertSeatId);

        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.of(reservation));

        ReservationInfoResponse response =
                reservationQueryService.getReservation(reservationId);

        assertThat(response.reservationId()).isEqualTo(reservation.getId());
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.concertSeatId()).isEqualTo(concertSeatId);
        assertThat(response.seatStatus()).isEqualTo(reservation.getStatus());
        assertThat(response.expiredAt()).isEqualTo(reservation.getExpiredAt());

        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void getReservation_fail() {
        Long notExistsReservationId = 1L;

        given(reservationRepository.findById(notExistsReservationId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationQueryService.getReservation(notExistsReservationId))
            .isInstanceOf(NotFoundReservationException.class);
    }
}