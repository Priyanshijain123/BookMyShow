package bookings.controller;

import bookings.payment.PaymentGatewayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class BookingExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> dataConflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap("error", "The requested theater or show conflicts with existing data"));
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<Map<String, String>> gatewayFailure(PaymentGatewayException exception) {
        return error(HttpStatus.BAD_GATEWAY, exception);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, RuntimeException exception) {
        return ResponseEntity.status(status).body(Collections.singletonMap("error", exception.getMessage()));
    }
}
