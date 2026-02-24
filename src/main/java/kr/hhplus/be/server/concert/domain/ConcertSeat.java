package kr.hhplus.be.server.concert.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class ConcertSeat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long concertDetailId;
    private int seatNo;
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    public static ConcertSeat create(Long concertDetailId, int seatNo) {
        ConcertSeat concertSeat = new ConcertSeat();
        concertSeat.concertDetailId = concertDetailId;
        concertSeat.seatNo = seatNo;
        concertSeat.status = SeatStatus.AVAILABLE;

        return concertSeat;
    }

    public ConcertSeat changeStatus(SeatStatus status) {
        this.status = status;
        return this;
    }
}
