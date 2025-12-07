package kr.hhplus.be.server.concert.domain;

import org.springframework.test.util.ReflectionTestUtils;

public class ConcertFixture {
    public static Concert createConcert() {
        Concert concert = Concert.create("title", "description");
        ReflectionTestUtils.setField(concert, "id", 1L);

        return concert;
    }

    public static Concert createConcert(Long id) {
        Concert concert = Concert.create("title", "description");
        ReflectionTestUtils.setField(concert, "id", id);

        return concert;
    }

    public static Concert createConcert(String title, String description) {
        Concert concert = Concert.create(title, description);
        ReflectionTestUtils.setField(concert, "id", 1L);

        return concert;
    }

    public static Concert createConcert(Long id, String title, String description) {
        Concert concert = Concert.create(title, description);
        ReflectionTestUtils.setField(concert, "id", id);

        return concert;
    }
}
