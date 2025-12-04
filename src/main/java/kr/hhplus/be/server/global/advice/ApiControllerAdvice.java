package kr.hhplus.be.server.global.advice;

import kr.hhplus.be.server.balance.domain.NotFoundBalanceException;
import kr.hhplus.be.server.concert.domain.NotFoundConcertException;
import kr.hhplus.be.server.reservation.domain.CannotChangeReservationStatusException;
import kr.hhplus.be.server.reservation.domain.NotFoundReservationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception){
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(NotFoundConcertException.class)
    public ProblemDetail notFoundConcertExceptionHandler(NotFoundConcertException exception){
        return getProblemDetail(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(NotFoundBalanceException.class)
    public ProblemDetail notFoundBalanceExceptionHandler(NotFoundBalanceException exception){
        return getProblemDetail(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(NotFoundReservationException.class)
    public ProblemDetail notFoundReservationExceptionHandler(NotFoundReservationException exception){
        return getProblemDetail(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(CannotChangeReservationStatusException.class)
    public ProblemDetail cannotChangeReservationStatusExceptionHandler(CannotChangeReservationStatusException exception){
        return getProblemDetail(HttpStatus.CONFLICT, exception);
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }
}
