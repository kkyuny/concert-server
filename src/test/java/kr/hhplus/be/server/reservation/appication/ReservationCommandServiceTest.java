package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.reservation.api.dto.ReservationChangeResponse;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.domain.CannotChangeReservationStatusException;
import kr.hhplus.be.server.reservation.domain.NotFoundReservationException;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrasturcture.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void changeReservationStatus_notFoundReservationTest() {
        // given
        Long reservationId = 1L;

        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.CONFIRMED))
            .isInstanceOf(NotFoundReservationException.class);
    }

    @Test
    void changeReservationStatus_pendingToConfirmTest() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;

        Reservation reservation = Reservation.create(userId, reservationId);

        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.of(reservation));

        // when
        ReservationStatus changedStatus = ReservationStatus.CONFIRMED;
        ReservationChangeResponse response =
                reservationCommandService.changeReservationStatus(reservationId, changedStatus);

        // then
        assertThat(response).isNotNull();
        assertThat(response.reservationStatus()).isEqualTo(reservation.getStatus());
    }

    @Test
    void changeReservationStatus_confirmToExpiredTest() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;

        Reservation reservation = Reservation.create(userId, reservationId);
        reservation.changeStatus(ReservationStatus.CONFIRMED);

        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.of(reservation));

        // when & then
        ReservationStatus changedStatus = ReservationStatus.EXPIRED;

        assertThatThrownBy(() -> reservationCommandService.changeReservationStatus(reservationId, changedStatus))
            .isInstanceOf(CannotChangeReservationStatusException.class);
    }
}