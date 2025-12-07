package kr.hhplus.be.server.concert.domain;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public class ConcertDetailFixture {
    public static ConcertDetail createConcertDetail() {
        Concert concert = ConcertFixture.createConcert();
        ConcertDetail concertDetail = ConcertDetail.create(concert, LocalDate.now(), 100);
        ReflectionTestUtils.setField(concertDetail, "id", 1L);

        return concertDetail;
    }

    public static ConcertDetail createConcertDetail(LocalDate localDate, int price) {
        Concert concert = ConcertFixture.createConcert(1L, "title", "description");
        ConcertDetail concertDetail = ConcertDetail.create(concert, localDate, price);
        ReflectionTestUtils.setField(concertDetail, "id", 1L);

        return concertDetail;
    }

    public static ConcertDetail createConcertDetail(Concert concert, Long id) {
        ConcertDetail concertDetail = ConcertDetail.create(concert, LocalDate.now(), 100);
        ReflectionTestUtils.setField(concertDetail, "id", id);

        return concertDetail;
    }

    public static ConcertDetail createConcertDetail(Concert concert, LocalDate localDate, int price) {
        ConcertDetail concertDetail = ConcertDetail.create(concert, localDate, price);
        ReflectionTestUtils.setField(concertDetail, "id", 1L);

        return concertDetail;
    }

    public static ConcertDetail createConcertDetail(Concert concert, Long id, LocalDate localDate, int price) {
        ConcertDetail concertDetail = ConcertDetail.create(concert, localDate, price);
        ReflectionTestUtils.setField(concertDetail, "id", id);

        return concertDetail;
    }
}
