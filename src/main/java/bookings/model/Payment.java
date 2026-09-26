package bookings.model;

import java.time.Instant;

public class Payment {
    private String paymentId;
    private String bookingId;
    private PaymentMethod method;
    private long amountMinor;
    private String currency;
    private String gatewayReference;
    private Instant createdAt;

    public Payment(String paymentId, String bookingId, PaymentMethod method, long amountMinor,
                   String currency, String gatewayReference, Instant createdAt) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.method = method;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.gatewayReference = gatewayReference;
        this.createdAt = createdAt;
    }

    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public PaymentMethod getMethod() { return method; }
    public long getAmountMinor() { return amountMinor; }
    public String getCurrency() { return currency; }
    public String getGatewayReference() { return gatewayReference; }
    public Instant getCreatedAt() { return createdAt; }
}
