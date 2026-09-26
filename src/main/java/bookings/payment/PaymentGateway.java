package bookings.payment;

import bookings.model.PaymentMethod;

public interface PaymentGateway {
    PaymentMethod method();

    String charge(String paymentId, long amountMinor, String currency);
}
