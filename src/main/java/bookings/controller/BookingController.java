package bookings.controller;

import bookings.model.BookingResponse;
import bookings.model.CreateBookingRequest;
import bookings.model.Reservation;
import bookings.model.PriceQuote;
import bookings.service.BookingSystem;
import bookings.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bookings")
public class BookingController {
    private final BookingSystem bookingSystem;
    private final PaymentService payments;

    public BookingController(BookingSystem bookingSystem, PaymentService payments) {
        this.bookingSystem = bookingSystem;
        this.payments = payments;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                  @RequestBody CreateBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking request is required");
        }
        Reservation reservation = bookingSystem.book(request.getShowtimeId(), request.getSeatIds(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(reservation));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse get(@PathVariable String bookingId) {
        return BookingResponse.from(bookingSystem.getReservation(bookingId));
    }

    @GetMapping("/{bookingId}/quote")
    public PriceQuote quote(@PathVariable("bookingId") String bookingId) {
        return payments.quote(bookingId);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancel(@PathVariable String bookingId) {
        payments.cancelUnpaidBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
