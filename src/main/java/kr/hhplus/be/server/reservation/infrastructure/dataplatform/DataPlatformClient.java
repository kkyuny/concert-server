package kr.hhplus.be.server.reservation.infrastructure.dataplatform;

import org.springframework.stereotype.Component;

@Component
public class DataPlatformClient {

    public void send(Long reservationId, Long userId, Long concertSeatId) {

        System.out.println("📡 DataPlatform 전송");
        System.out.println("reservationId = " + reservationId);
        System.out.println("userId = " + userId);
    }
}