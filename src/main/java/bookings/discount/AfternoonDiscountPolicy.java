package bookings.discount;

import java.math.BigDecimal;

public  class AfternoonDiscountPolicy implements DiscountPolicy {
        private static final BigDecimal TWENTY_PERCENT = new BigDecimal("0.20");

        public String code() { return "AFTERNOON"; }

        public BigDecimal discount(int ticketNumber, int showLocalHour, BigDecimal currentPrice) {
            return showLocalHour >= 12 && showLocalHour < 17
                    ? currentPrice.multiply(TWENTY_PERCENT) : BigDecimal.ZERO;
        }
    }

