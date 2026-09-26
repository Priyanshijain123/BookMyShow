package bookings.model;

public class PriceQuote {
    private final String bookingId;
    private final long baseAmountMinor;
    private final long discountAmountMinor;
    private final long totalAmountMinor;
    private final String currency;

    public PriceQuote(String bookingId, long baseAmountMinor, long discountAmountMinor,
                      long totalAmountMinor, String currency) {
        this.bookingId = bookingId;
        this.baseAmountMinor = baseAmountMinor;
        this.discountAmountMinor = discountAmountMinor;
        this.totalAmountMinor = totalAmountMinor;
        this.currency = currency;
    }

    public String getBookingId() { return bookingId; }
    public long getTotalAmountMinor() { return totalAmountMinor; }
}
