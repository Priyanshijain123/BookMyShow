package bookings.discount;

import java.math.BigDecimal;

public class ThirdTicketDiscountPolicy implements DiscountPolicy {
    private static final BigDecimal HALF = new BigDecimal("0.50");

    public String code() { return "THIRD_TICKET"; }

    public BigDecimal discount(int ticketNumber, int showLocalHour, BigDecimal currentPrice) {
        return ticketNumber % 3 == 0 ? currentPrice.multiply(HALF) : BigDecimal.ZERO;
    }
}
