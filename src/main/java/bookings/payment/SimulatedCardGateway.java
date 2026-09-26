package bookings.payment;

import bookings.model.PaymentMethod;

public class SimulatedCardGateway implements PaymentGateway {
    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    public String charge(String paymentId, long amountMinor, String currency) {
        return "SIM-CARD-" + paymentId;
    }
}
