package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.TestKafkaConfiguration;
import kr.hhplus.be.server.TestRedisConfiguration;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrastructure.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true", // 이름이 겹쳐도 테스트 빈으로 덮어쓰기 허용
        "spring.kafka.listener.auto-startup=false"           // 테스트 시 카프카 서버 연결 시도 방지
})
@ActiveProfiles("test")
@Import({TestRedisConfiguration.class, TestKafkaConfiguration.class})
class ReservationExpireTest {

    @Autowired
    private ReservationExpireService reservationExpireService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 만료시간_지난_예약은_EXPIRED로_변경된다() {
        // given
        Reservation reservation = reservationRepository.save(
                Reservation.create(
                        1L,
                        1L,
                        LocalDateTime.now().minusMinutes(10) // 이미 만료
                )
        );

        // when
        reservationExpireService.expireReservations();

        // then
        Reservation updated =
                reservationRepository.findById(reservation.getId())
                        .orElseThrow();

        assertThat(updated.getStatus())
                .isEqualTo(ReservationStatus.EXPIRED);
    }
}