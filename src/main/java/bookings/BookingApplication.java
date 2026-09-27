package bookings;

import bookings.payment.PaymentGateway;
import bookings.payment.SimulatedCardGateway;
import bookings.payment.SimulatedUpiGateway;
import bookings.service.BookingSystem;
import bookings.service.PaymentService;
import bookings.service.BookingEventPublisher;
import bookings.service.SqsBookingEventPublisher;
import bookings.service.SeatBookingService;
import bookings.service.TheaterCatalogService;
import bookings.service.TicketPricingService;
import bookings.discount.ThirdTicketDiscountPolicy;
import bookings.discount.AfternoonDiscountPolicy;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class BookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }

    @Bean
    BookingSystem bookingSystem(BookingEventPublisher publisher, SeatBookingService seatBookingService,
                                TheaterCatalogService theaterCatalogService) {
        return new BookingSystem(publisher, seatBookingService, theaterCatalogService);
    }

    @Bean
    ApplicationRunner initializeCatalog(BookingSystem bookingSystem) {
        return args -> bookingSystem.initializeCatalog();
    }

    @Bean(destroyMethod = "close")
    BookingEventPublisher bookingEventPublisher(@Value("${booking.sqs.queue-url:}") String queueUrl,
                                               ObjectMapper objectMapper) {
        if (queueUrl.isBlank()) {
            return reservation -> { };
        }
        return new SqsBookingEventPublisher(SqsClient.create(), queueUrl, objectMapper);
    }

    @Bean
    PaymentGateway cardPaymentGateway() {
        return new SimulatedCardGateway();
    }

    @Bean
    PaymentGateway upiPaymentGateway() {
        return new SimulatedUpiGateway();
    }

    @Bean
    TicketPricingService ticketPricingService(@Value("${booking.ticket-price-minor:20000}") long ticketPriceMinor,
            @Value("${booking.discount.eligible-cities:Mumbai}") String cities,
            @Value("${booking.discount.eligible-theaters:}") String theaterIds) {
        Set<String> selectedCities = Arrays.stream(cities.split(",")).collect(Collectors.toSet());
        Set<String> selectedTheaters = Arrays.stream(theaterIds.split(",")).collect(Collectors.toSet());
        return new TicketPricingService(ticketPriceMinor, selectedCities, selectedTheaters,
                Arrays.asList(new ThirdTicketDiscountPolicy(), new AfternoonDiscountPolicy()));
    }

    @Bean
    PaymentService paymentService(BookingSystem bookingSystem, TicketPricingService ticketPricingService,
                                  List<PaymentGateway> gateways) {
        return new PaymentService(bookingSystem, ticketPricingService, gateways);
    }
}
