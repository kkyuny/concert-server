package kr.hhplus.be.server.reservation.appication;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationExpireScheduler {

    private final ReservationExpireService reservationExpireService;

    // 1분마다 실행
    @Scheduled(fixedRate = 60000)
    public void run() {
        reservationExpireService.expireReservations();
    }
}
