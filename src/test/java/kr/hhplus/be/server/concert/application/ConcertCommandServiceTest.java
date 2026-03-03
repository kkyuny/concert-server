package kr.hhplus.be.server.concert.application;

import kr.hhplus.be.server.concert.api.dto.ConcertSeatStatusResponse;
import kr.hhplus.be.server.concert.domain.CannotChangeSeatStatusException;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@Disabled("레디스 추가 사용으로 단위 테스트 비활성화")
@ExtendWith(MockitoExtension.class)
class ConcertCommandServiceTest {

    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ConcertCommandService concertCommandService;

    @BeforeEach
    void setUp() {
        // opsForValue() 호출 시 mock 반환
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        // setIfAbsent 호출 시 항상 true 반환 (락 획득 성공)
        given(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).willReturn(true);
    }

    @Test
    void changeSeatStatus_availableToHold_success() {
        ConcertSeat concertSeat = ConcertSeat.create(1L, 1);
        given(concertSeatRepository.findByIdForLock(anyLong())).willReturn(Optional.of(concertSeat));

        ConcertSeatStatusResponse response = concertCommandService.changeConcertSeatStatus(1L, SeatStatus.HOLD);

        assertThat(response.seatStatus()).isEqualTo(SeatStatus.HOLD);
    }

    @Test
    void changeSeatStatus_holdToReserved_success() {
        ConcertSeat concertSeat = ConcertSeat.create(1L, 1);
        concertSeat.changeStatus(SeatStatus.HOLD);
        given(concertSeatRepository.findByIdForLock(anyLong())).willReturn(Optional.of(concertSeat));

        ConcertSeatStatusResponse response = concertCommandService.changeConcertSeatStatus(1L, SeatStatus.RESERVED);

        assertThat(response.seatStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    void changeSeatStatus_holdToAvailable_fail() {
        ConcertSeat concertSeat = ConcertSeat.create(1L, 1);
        concertSeat.changeStatus(SeatStatus.HOLD);
        given(concertSeatRepository.findByIdForLock(anyLong())).willReturn(Optional.of(concertSeat));

        assertThatThrownBy(() ->
                concertCommandService.changeConcertSeatStatus(1L, SeatStatus.AVAILABLE))
                .isInstanceOf(CannotChangeSeatStatusException.class);
    }

    @Test
    void changeSeatStatus_holdToHold_fail() {
        ConcertSeat concertSeat = ConcertSeat.create(1L, 1);
        concertSeat.changeStatus(SeatStatus.HOLD);
        given(concertSeatRepository.findByIdForLock(anyLong())).willReturn(Optional.of(concertSeat));

        assertThatThrownBy(() ->
                concertCommandService.changeConcertSeatStatus(1L, SeatStatus.HOLD))
                .isInstanceOf(CannotChangeSeatStatusException.class);
    }

    @Test
    void changeSeatStatus_reservedToAny_fail() {
        ConcertSeat concertSeat = ConcertSeat.create(1L, 1);
        concertSeat.changeStatus(SeatStatus.RESERVED);
        given(concertSeatRepository.findByIdForLock(anyLong())).willReturn(Optional.of(concertSeat));

        assertThatThrownBy(() ->
                concertCommandService.changeConcertSeatStatus(1L, SeatStatus.HOLD))
                .isInstanceOf(CannotChangeSeatStatusException.class);
    }
}