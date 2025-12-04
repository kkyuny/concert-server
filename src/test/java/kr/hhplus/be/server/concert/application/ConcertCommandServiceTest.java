package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.domain.CannotChangeSeatStatusException;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConcertCommandServiceTest {
    @Mock
    private ConcertSeatRepository concertSeatRepository;
    
    @InjectMocks
    private ConcertCommandService concertCommandService;

    @Test
    void changeSeatStatus_availableToHold_success() {
        Long concertDetailId = 1L;
        Long concertSeatId = 1L;
        int seatNo = 1;
        SeatStatus changeSeatStatus = SeatStatus.HOLD;

        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);

        given(concertSeatRepository.findById(anyLong()))
                .willReturn(Optional.of(concertSeat));

        ConcertSeatStatusResponse concertSeatStatusResponse = concertCommandService
                .changeConcertSeatStaus(concertSeatId, changeSeatStatus);

        assertThat(concertSeatStatusResponse.seatStatus()).isEqualTo(changeSeatStatus);
    }

    @Test
    void changeSeatStatus_holdToReserved_success() {
        Long concertDetailId = 1L;
        Long concertSeatId = 1L;
        int seatNo = 1;
        SeatStatus changeSeatStatus = SeatStatus.RESERVED;

        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
        concertSeat.changeStatus(SeatStatus.HOLD);

        given(concertSeatRepository.findById(anyLong()))
                .willReturn(Optional.of(concertSeat));

        ConcertSeatStatusResponse concertSeatStatusResponse = concertCommandService
                .changeConcertSeatStaus(concertSeatId, changeSeatStatus);

        assertThat(concertSeatStatusResponse.seatStatus()).isEqualTo(changeSeatStatus);
    }

    @Test
    void changeSeatStatus_holdToAvailable_fail() {
        Long concertDetailId = 1L;
        Long concertSeatId = 1L;
        int seatNo = 1;
        SeatStatus changeSeatStatus = SeatStatus.AVAILABLE;

        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
        concertSeat.changeStatus(SeatStatus.HOLD);

        given(concertSeatRepository.findById(anyLong()))
                .willReturn(Optional.of(concertSeat));

        assertThatThrownBy(() ->
                concertCommandService.changeConcertSeatStaus(concertSeatId, changeSeatStatus))
            .isInstanceOf(CannotChangeSeatStatusException.class);
    }

    @Test
    void changeSeatStatus_holdToHold_fail() {
        Long concertDetailId = 1L;
        Long concertSeatId = 1L;
        int seatNo = 1;
        SeatStatus changeSeatStatus = SeatStatus.HOLD;

        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
        concertSeat.changeStatus(SeatStatus.HOLD);

        given(concertSeatRepository.findById(anyLong()))
                .willReturn(Optional.of(concertSeat));

        assertThatThrownBy(() ->
                concertCommandService.changeConcertSeatStaus(concertSeatId, changeSeatStatus))
                .isInstanceOf(CannotChangeSeatStatusException.class);
    }

    @Test
    void changeSeatStatus_reservedToAny_fail() {
        Long concertDetailId = 1L;
        Long concertSeatId = 1L;
        int seatNo = 1;
        //SeatStatus changeSeatStatus = SeatStatus.AVAILABLE;
        SeatStatus changeSeatStatus = SeatStatus.HOLD;

        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
        concertSeat.changeStatus(SeatStatus.RESERVED);

        given(concertSeatRepository.findById(anyLong()))
                .willReturn(Optional.of(concertSeat));

        assertThatThrownBy(() ->
                concertCommandService.changeConcertSeatStaus(concertSeatId, changeSeatStatus))
                .isInstanceOf(CannotChangeSeatStatusException.class);
    }
}