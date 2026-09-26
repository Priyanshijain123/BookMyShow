package bookings.service;

import bookings.discount.DiscountPolicy;
import bookings.model.PriceQuote;
import bookings.model.Reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class TicketPricingService {
    private final long ticketPriceMinor;
    private final Set<String> eligibleCities;
    private final Set<String> eligibleTheaterIds;
    private final List<DiscountPolicy> discountPolicies;

    public TicketPricingService(long ticketPriceMinor, Set<String> eligibleCities,
                                Set<String> eligibleTheaterIds, List<DiscountPolicy> discountPolicies) {
        if (ticketPriceMinor <= 0) {
            throw new IllegalArgumentException("Ticket price must be positive");
        }
        this.ticketPriceMinor = ticketPriceMinor;
        this.eligibleCities = eligibleCities.stream().map(this::normalize).collect(Collectors.toSet());
        this.eligibleTheaterIds = eligibleTheaterIds.stream().map(this::normalize).collect(Collectors.toSet());
        this.discountPolicies = discountPolicies;
    }

    public PriceQuote quote(Reservation reservation) {
        int ticketCount = reservation.getSeatIds().size();
        long baseAmount = Math.multiplyExact(ticketPriceMinor, ticketCount);
        long totalAmount = 0;
        boolean eligible = eligibleCities.contains(normalize(reservation.getShowtime().getTheater().getCity()))
                && eligibleTheaterIds.contains(normalize(reservation.getShowtime().getTheater().getId()));
        int showHour = reservation.getShowtime().getDatetime().getHour();

        for (int ticketNumber = 1; ticketNumber <= ticketCount; ticketNumber++) {
            BigDecimal price = BigDecimal.valueOf(ticketPriceMinor);
            if (eligible) {
                for (DiscountPolicy policy : discountPolicies) {
                    BigDecimal discount = policy.discount(ticketNumber, showHour, price)
                            .setScale(0, RoundingMode.HALF_UP);
                    price = price.subtract(discount);
                }
            }
            totalAmount = Math.addExact(totalAmount, price.longValueExact());
        }

        return new PriceQuote(reservation.getBookingId(), baseAmount,
                baseAmount - totalAmount, totalAmount, "INR");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
