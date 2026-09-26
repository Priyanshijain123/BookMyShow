package bookings.service;

import bookings.model.Reservation;

public interface BookingEventPublisher extends AutoCloseable {
    void publish(Reservation reservation);

    @Override
    default void close() { }
}
