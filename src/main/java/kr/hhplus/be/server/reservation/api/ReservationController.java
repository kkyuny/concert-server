package kr.hhplus.be.server.reservation.api;

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

    @PostMapping
    public ReservationResponse reserveConcert(@RequestBody ReservationRequest reservationRequest) {
        return reservationFacade.initReservation(reservationRequest.concertSeatId(), reservationRequest.userId());
    }
}
