package kr.hhplus.be.server.reservation.infrastructure.dataplatform;

import kr.hhplus.be.server.reservation.event.ReservationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationEventListener {

    private final DataPlatformClient dataPlatformClient;

    @EventListener
    public void handle(ReservationCreatedEvent event) {

        try {
            dataPlatformClient.send(
                    event.reservationId(),
                    event.userId(),
                    event.concertId(),
                    event.seatId()
            );
        } catch (Exception e) {
            log.error("DataPlatform 전송 실패", e);
        }
    }
}