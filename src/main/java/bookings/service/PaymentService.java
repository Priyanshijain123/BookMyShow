package bookings.service;

import bookings.model.Payment;
import bookings.model.PaymentMethod;
import bookings.model.PriceQuote;
import bookings.payment.PaymentGateway;
import bookings.payment.PaymentGatewayException;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentService {
    private static final String CURRENCY = "INR";

    private final BookingSystem bookingSystem;
    private final TicketPricingService ticketPricingService;
    private final Map<PaymentMethod, PaymentGateway> gateways = new EnumMap<>(PaymentMethod.class);
    private final Map<String, Payment> paymentsByBookingId = new ConcurrentHashMap<>();

    public PaymentService(BookingSystem bookingSystem, TicketPricingService ticketPricingService,
                          List<PaymentGateway> gatewayStrategies) {
        this.bookingSystem = bookingSystem;
        this.ticketPricingService = ticketPricingService;
        for (PaymentGateway gateway : gatewayStrategies) {
            if (gateways.putIfAbsent(gateway.method(), gateway) != null) {
                throw new IllegalArgumentException("Duplicate payment gateway for " + gateway.method());
            }
        }
    }

    public PriceQuote quote(String bookingId) {
        return ticketPricingService.quote(bookingSystem.getReservation(bookingId));
    }

    public Payment pay(String bookingId, PaymentMethod method, long amountMinor) {
        if (bookingId == null || bookingId.isEmpty() || method == null || amountMinor <= 0) {
            throw new IllegalArgumentException("Booking ID, payment method, and positive amount are required");
        }
        PaymentGateway gateway = gateways.get(method);
        if (gateway == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }

        Payment payment = paymentsByBookingId.computeIfAbsent(bookingId, id -> {
            PriceQuote quote = quote(id);
            if (amountMinor != quote.getTotalAmountMinor()) {
                throw new IllegalArgumentException("Payment amount must equal the quoted total: "
                        + quote.getTotalAmountMinor());
            }
            String paymentId = UUID.randomUUID().toString();
            String gatewayReference;
            try {
                gatewayReference = gateway.charge(paymentId, amountMinor, CURRENCY);
            } catch (RuntimeException exception) {
                throw new PaymentGatewayException("Payment gateway charge failed", exception);
            }
            if (gatewayReference == null || gatewayReference.isEmpty()) {
                throw new PaymentGatewayException("Payment gateway returned no reference");
            }
            return new Payment(paymentId, id, method, amountMinor, CURRENCY, gatewayReference, Instant.now());
        });

        if (payment.getMethod() != method || payment.getAmountMinor() != amountMinor) {
            throw new IllegalStateException("Booking already has a payment with different details");
        }
        return payment;
    }

    public void cancelUnpaidBooking(String bookingId) {
        if (bookingId == null || bookingId.isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        paymentsByBookingId.compute(bookingId, (id, payment) -> {
            if (payment != null) {
                throw new IllegalStateException("Paid booking requires a refund before cancellation");
            }
            bookingSystem.cancelReservation(id);
            return null;
        });
    }

    public Payment getPayment(String bookingId) {
        if (bookingId == null || bookingId.isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        Payment payment = paymentsByBookingId.get(bookingId);
        if (payment == null) {
            throw new NoSuchElementException("Payment not found for booking: " + bookingId);
        }
        return payment;
    }
}
