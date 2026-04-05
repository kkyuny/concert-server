package kr.hhplus.be.server.reservation.api;

import kr.hhplus.be.server.queue.application.QueueService;
import kr.hhplus.be.server.reservation.api.dto.ReservationRequest;
import kr.hhplus.be.server.reservation.api.dto.ReservationResponse;
import kr.hhplus.be.server.reservation.facade.ReservationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationFacade reservationFacade;
    private final QueueService queueService;

    @PostMapping
    public ReservationResponse reserveConcert(@RequestBody ReservationRequest request) {
        boolean permit = queueService.waitForPermit(request.userId().toString(), 3);
        if (!permit) throw new RuntimeException("대기시간 초과");

        try {
            // 슬롯 확보되면 Facade 호출 → 실제 예약 처리
            return reservationFacade.initReservation(
                    request.concertSeatId(),
                    request.userId(),
                    request.token(),
                    request.concertId()
            );
        } finally {
            queueService.releaseSlot(); // 슬롯 반환
        }
    }
}
