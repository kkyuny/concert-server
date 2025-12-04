package kr.hhplus.be.server.reservation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long concertSeatId;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private LocalDateTime expiredAt;

    public static Reservation create(Long userId, Long concertSeatId) {
        Reservation reservation = new Reservation();

        reservation.userId = userId;
        reservation.concertSeatId = concertSeatId;
        reservation.status = ReservationStatus.PENDING;
        reservation.expiredAt = LocalDateTime.now().plusMinutes(5);

        return reservation;
    }

    public Reservation changeStatus(ReservationStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new CannotChangeReservationStatusException(this.id);
        }

        this.status = nextStatus;

        return this;
    }

    public void expire() {
        changeStatus(ReservationStatus.EXPIRED);
    }
}
