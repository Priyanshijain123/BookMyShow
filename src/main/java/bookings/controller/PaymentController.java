package bookings.controller;

import bookings.model.Payment;
import bookings.model.PaymentMethod;
import bookings.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/bookings/{bookingId}/payments")
public class PaymentController {
    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@PathVariable String bookingId, @RequestBody PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }
        Payment payment = payments.pay(bookingId, request.method(), request.amountMinor());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping
    public PaymentResponse get(@PathVariable String bookingId) {
        return PaymentResponse.from(payments.getPayment(bookingId));
    }

    public record PaymentRequest(PaymentMethod method, long amountMinor) {}

    public record PaymentResponse(String paymentId, String bookingId, PaymentMethod method,
                                  long amountMinor, String currency, String status,
                                  String gatewayReference, Instant createdAt) {
        static PaymentResponse from(Payment payment) {
            return new PaymentResponse(payment.getPaymentId(), payment.getBookingId(), payment.getMethod(),
                    payment.getAmountMinor(), payment.getCurrency(), "SUCCEEDED",
                    payment.getGatewayReference(), payment.getCreatedAt());
        }
    }
}
