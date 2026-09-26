package bookings.payment;

import bookings.model.PaymentMethod;

public class SimulatedUpiGateway implements PaymentGateway {
    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }

    @Override
    public String charge(String paymentId, long amountMinor, String currency) {
        return "SIM-UPI-" + paymentId;
    }
}
