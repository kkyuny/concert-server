package kr.hhplus.be.server.concert.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor
@Entity
public class ConcertDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate concertDate;
    private int price;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    public static ConcertDetail create(Concert concert, LocalDate concertDate, int price) {
        ConcertDetail detail = new ConcertDetail();

        detail.concert = concert;
        detail.concertDate = concertDate;
        detail.price = price;

        return detail;
    }
}
