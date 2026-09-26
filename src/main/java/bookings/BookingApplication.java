package bookings;

import bookings.model.Theater;
import bookings.model.Movie;
import bookings.payment.PaymentGateway;
import bookings.payment.SimulatedCardGateway;
import bookings.payment.SimulatedUpiGateway;
import bookings.service.BookingSystem;
import bookings.service.PaymentService;
import bookings.service.Showtime;
import bookings.service.BookingEventPublisher;
import bookings.service.SqsBookingEventPublisher;
import bookings.service.TicketPricingService;
import bookings.discount.ThirdTicketDiscountPolicy;
import bookings.discount.AfternoonDiscountPolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@SpringBootApplication
public class BookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }

    @Bean
    BookingSystem bookingSystem(ObjectProvider<Theater> theaters, BookingEventPublisher publisher) {
        return new BookingSystem(theaters.orderedStream().collect(Collectors.toList()), publisher);
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
    Theater sampleTheater() {
        Theater theater = new Theater("theater-1", "City Cinema", "Mumbai");
        Movie movie = new Movie("movie-1", "Sample Movie", "Hindi");
        theater.getShowtimes().add(new Showtime("show-1", theater, movie,
                LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0), "Screen 1"));
        return theater;
    }

    @Bean
    Theater secondSampleTheater() {
        Theater theater = new Theater("theater-2", "Capital Cinema", "Delhi");
        Movie movie = new Movie("movie-2", "Example Adventure", "English");
        theater.getShowtimes().add(new Showtime("show-2", theater, movie,
                LocalDateTime.now().plusDays(2).withHour(18).withMinute(0).withSecond(0).withNano(0), "Screen 1"));
        return theater;
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
            @Value("${booking.discount.eligible-theaters:theater-1}") String theaterIds) {
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
