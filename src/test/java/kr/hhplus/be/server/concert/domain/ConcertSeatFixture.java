package kr.hhplus.be.server.concert.domain;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConcertSeatFixture {
    public static ConcertSeat createConcertSeat(Long concertDetailId, int seatNo) {
        ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
        ReflectionTestUtils.setField(concertSeat, "id", (long)seatNo);

        return concertSeat;
    }

    public static List<ConcertSeat> createConcertSeats(Long concertDetailId, int seatNoRange) {
        List<ConcertSeat> concertSeats = new ArrayList<>();

        for(int seatNo=0; seatNo<seatNoRange; seatNo++) {
            ConcertSeat concertSeat = ConcertSeat.create(concertDetailId, seatNo);
            ReflectionTestUtils.setField(concertSeat, "id", (long)seatNo);

            concertSeats.add(concertSeat);
        }

        return concertSeats;
    }
}
