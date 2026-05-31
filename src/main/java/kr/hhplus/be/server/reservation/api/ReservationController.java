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
    public ReservationResponse reserve(
            @RequestBody ReservationRequest request
    ) {
        boolean acquired =
                queueService.tryAcquire(
                        request.userId()
                );

        if (!acquired) {
            throw new IllegalStateException(
                    "대기 순서가 아닙니다."
            );
        }

        try {

            return reservationFacade.initReservation(
                    request.concertSeatId(),
                    request.userId(),
                    request.token()
            );
        } finally {
            queueService.release();
        }
    }
}