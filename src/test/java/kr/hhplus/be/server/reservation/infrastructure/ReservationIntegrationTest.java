package kr.hhplus.be.server.reservation.infrastructure;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDetail;
import kr.hhplus.be.server.concert.domain.ConcertSeat;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.concert.infrastructure.ConcertDetailRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertRepository;
import kr.hhplus.be.server.concert.infrastructure.ConcertSeatRepository;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.appication.ReservationTokenService;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.facade.ReservationFacade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReservationIntegrationTest {
    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDetailRepository concertDetailRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTokenService reservationTokenService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void clearRedis() {
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());
        redisTemplate.getConnectionFactory()
                .getConnection()
                .flushAll();
    }

    @Test
    void initReservation_changesSeatAndCreatesReservation() {

        // given
        Concert concert = concertRepository.save(Concert.create("아이유 콘서트", ""));
        ConcertDetail detail = concertDetailRepository.save(
                ConcertDetail.create(concert, LocalDate.of(2026, 3, 1), 500)
        );
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.create(detail.getId(), 1)
        );

        Long userId = 100L;

        String token =
                reservationTokenService.issueToken(userId, seat.getId());

        // when
        ReservationResponse response = reservationFacade.initReservation(seat.getId(), userId, token, concert.getId());

        // then
        // 1. 좌석 상태 변경 확인
        ConcertSeat updatedSeat = concertSeatRepository.findById(seat.getId()).orElseThrow();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.HOLD);

        // 2. 예약 생성 확인
        assertThat(response).isNotNull();
        assertThat(response.seatStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservationRepository.findById(response.reservationId()))
                .isPresent();
    }
}