package kr.hhplus.be.server.reservation.appication;

import kr.hhplus.be.server.reservation.api.dto.ReservationInfoResponse;
import kr.hhplus.be.server.reservation.domain.NotFoundReservationException;
import kr.hhplus.be.server.reservation.infrastructure.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationQueryService {
    private final ReservationRepository reservationRepository;

    public ReservationInfoResponse getReservation(Long reservationId){
        return ReservationInfoResponse.of(reservationRepository.findByIdWithLock(reservationId).orElseThrow(
                () -> new NotFoundReservationException(reservationId)));
    }
}
