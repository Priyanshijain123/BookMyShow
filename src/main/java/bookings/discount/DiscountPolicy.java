package bookings.discount;

import java.math.BigDecimal;

public interface DiscountPolicy {

        String code();
        BigDecimal discount(int ticketNumber, int showLocalHour, BigDecimal currentPrice);
    }


