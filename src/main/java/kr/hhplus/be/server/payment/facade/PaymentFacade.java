package kr.hhplus.be.server.payment.facade;

import kr.hhplus.be.server.concert.application.ConcertCommandService;
import kr.hhplus.be.server.concert.application.ConcertQueryService;
import kr.hhplus.be.server.concert.domain.SeatStatus;
import kr.hhplus.be.server.payment.api.dto.PaymentResponse;
import kr.hhplus.be.server.payment.application.PaymentCommandService;
import kr.hhplus.be.server.reservation.api.dto.ReservationInfoResponse;
import kr.hhplus.be.server.reservation.appication.ReservationCommandService;
import kr.hhplus.be.server.reservation.appication.ReservationQueryService;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

@RequiredArgsConstructor
@Transactional
@Service
public class PaymentFacade {
    private final PaymentCommandService paymentCommandService;
    private final ConcertCommandService concertCommandService;
    private final ConcertQueryService concertQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration DAILY_TTL = Duration.ofDays(1);   // 하루 + 여유 1일
    private static final Duration WEEKLY_TTL = Duration.ofDays(7);

    @Transactional
    public PaymentResponse executePayment(Long reservationId, Long amount) {
        ReservationInfoResponse reservationInfoResponse = reservationQueryService.getReservation(reservationId);
        // 결제 실패
        if (reservationInfoResponse.expiredAt().isBefore(LocalDateTime.now())
                || reservationInfoResponse.seatStatus() != ReservationStatus.PENDING) {
            reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.EXPIRED);
            concertCommandService.changeConcertSeatStatus(reservationInfoResponse.concertSeatId(), SeatStatus.AVAILABLE);

            return paymentCommandService.createFailPayment(reservationInfoResponse.userId(), reservationId, amount);
        }

        // 결제 성공
        reservationCommandService.changeReservationStatus(reservationId, ReservationStatus.CONFIRMED);
        concertCommandService.changeConcertSeatStatus(reservationInfoResponse.concertSeatId(), SeatStatus.RESERVED);

        LocalDateTime now = LocalDateTime.now();

        /** ================== DAILY ================== **/
        LocalDateTime tomorrow = now.toLocalDate().plusDays(1).atStartOfDay();
        Duration dailyTtl = Duration.between(now, tomorrow);

        String dailyKey = "concert:ranking:daily:" + now.toLocalDate();

        redisTemplate.opsForZSet().incrementScore(dailyKey, reservationInfoResponse.concertId(), 1);
        redisTemplate.expire(dailyKey, dailyTtl);

        /** ================== WEEKLY ================== **/
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        // 이번 주 정보
        int weekOfYear = now.get(weekFields.weekOfWeekBasedYear());
        int year = now.getYear();

        // 다음 주 시작 (월요일 00:00)
        LocalDate nextWeekStart = now.toLocalDate()
                .with(weekFields.dayOfWeek(), 1) // 이번 주 월요일
                .plusWeeks(1);

        LocalDateTime nextWeekStartTime = nextWeekStart.atStartOfDay();
        Duration weeklyTtl = Duration.between(now, nextWeekStartTime);

        String weeklyKey = "concert:ranking:weekly:" + year + "-W" + weekOfYear;

        redisTemplate.opsForZSet().incrementScore(weeklyKey, reservationInfoResponse.concertId(), 1);
        redisTemplate.expire(weeklyKey, weeklyTtl);

        return paymentCommandService.createPayment(reservationInfoResponse.userId(), reservationId, amount);
    }

    // 현재 날짜 기준 연도-주차 문자열 생성 (예: "2026-W11")
    private String getYearWeek(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int week = date.get(weekFields.weekOfWeekBasedYear());
        return date.getYear() + "-W" + week;
    }
}
