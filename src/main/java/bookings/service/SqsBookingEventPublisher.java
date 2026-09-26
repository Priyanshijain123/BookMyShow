package bookings.service;

import bookings.model.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.LinkedHashMap;
import java.util.Map;

public class SqsBookingEventPublisher implements BookingEventPublisher {
    private final SqsClient client;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    public SqsBookingEventPublisher(SqsClient client, String queueUrl, ObjectMapper objectMapper) {
        this.client = client;
        this.queueUrl = queueUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(Reservation reservation) {
        String body;
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventType", "BOOKING_CREATED");
            event.put("bookingId", reservation.getBookingId());
            event.put("showtimeId", reservation.getShowtime().getId());
            event.put("seatIds", reservation.getSeatIds());
            body = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize booking event", exception);
        }
        client.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build());
    }

    @Override
    public void close() {
        client.close();
    }
}
