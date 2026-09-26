package bookings.service;
import bookings.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingSystem {
        private static final Logger LOGGER = Logger.getLogger(BookingSystem.class.getName());

        private final List<Theater> theaters;
        private final BookingEventPublisher bookingEventPublisher;
        private final Map<String, Showtime> showtimesById;
        private final Map<String, Reservation> reservationsById;
        private final Map<String, IdempotencySlot> bookingsByIdempotencyKey;

        public BookingSystem(List<Theater> theaters) {
            this(theaters, reservation -> { });
        }

        public BookingSystem(List<Theater> theaters, BookingEventPublisher bookingEventPublisher) {
            this.theaters = theaters;
            this.bookingEventPublisher = Objects.requireNonNull(bookingEventPublisher);
            this.showtimesById = new HashMap<>();
            this.reservationsById = new ConcurrentHashMap<>();
            this.bookingsByIdempotencyKey = new ConcurrentHashMap<>();

            for (Theater theater : theaters) {
                for (Showtime showtime : theater.getShowtimes()) {
                    showtimesById.put(showtime.getId(), showtime);
                }
            }
        }


        public List<Showtime> searchMovies(String title, String language, String city) {
            String titleFilter = normalize(title);
            String languageFilter = normalize(language);
            String cityFilter = normalize(city);

            List<Showtime> results = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Showtime showtime : showtimesById.values()) {
                if (showtime.getDatetime().isAfter(now)
                        && (titleFilter.isEmpty() || normalize(showtime.getMovie().getTitle()).contains(titleFilter))
                        && (languageFilter.isEmpty() || normalize(showtime.getMovie().getLanguage()).equals(languageFilter))
                        && (cityFilter.isEmpty() || normalize(showtime.getTheater().getCity()).equals(cityFilter))) {
                    results.add(showtime);
                }
            }

            return results;
        }

        public List<Theater> searchTheaters(String city) {
            String cityFilter = normalize(city);
            if (cityFilter.isEmpty()) {
                throw new IllegalArgumentException("City is required");
            }

            List<Theater> results = new ArrayList<>();
            for (Theater theater : theaters) {
                if (normalize(theater.getCity()).equals(cityFilter)) {
                    results.add(theater);
                }
            }
            return results;
        }

        private String normalize(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        }

        public List<Showtime> getShowtimesAtTheater(Theater theater) {
            if (theater == null) {
                return new ArrayList<>();
            }

            List<Showtime> results = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Showtime showtime : theater.getShowtimes()) {
                if (showtime.getDatetime().isAfter(now)) {
                    results.add(showtime);
                }
            }

            return results;
        }

        public List<Showtime> getShowtimesAtTheater(String theaterId) {
            if (theaterId == null || theaterId.isEmpty()) {
                throw new IllegalArgumentException("Theater ID is required");
            }
            for (Theater theater : theaters) {
                if (theater.getId().equals(theaterId)) {
                    return getShowtimesAtTheater(theater);
                }
            }
            throw new NoSuchElementException("Theater not found: " + theaterId);
        }

        public Reservation bookReservation(String showtimeId, List<String> seatIds) {
            if (showtimeId == null || seatIds == null || seatIds.isEmpty()) {
                throw new IllegalArgumentException("Invalid booking request");
            }

            Showtime showtime = showtimesById.get(showtimeId);
            if (showtime == null) {
                throw new NoSuchElementException("Showtime not found: " + showtimeId);
            }

            Reservation reservation = new Reservation(
                    UUID.randomUUID().toString(),
                    showtime,
                    seatIds
            );

            showtime.book(reservation);

            reservationsById.put(reservation.getBookingId(), reservation);

            try {
                bookingEventPublisher.publish(reservation);
            } catch (RuntimeException exception) {
                LOGGER.log(Level.WARNING, "Booking created, but SQS notification failed", exception);
            }

            return reservation;
        }

        public Reservation book(String showtimeId, List<String> seatIds, String idempotencyKey) {
            if (idempotencyKey == null || idempotencyKey.isEmpty()) {
                throw new IllegalArgumentException("Idempotency-Key is required");
            }
            if (showtimeId == null || showtimeId.isEmpty()
                    || seatIds == null || seatIds.isEmpty() || seatIds.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Invalid booking request");
            }

            BookingInput input = new BookingInput(showtimeId, seatIds);
            IdempotencySlot newSlot = new IdempotencySlot(input);
            IdempotencySlot existingSlot = bookingsByIdempotencyKey.putIfAbsent(idempotencyKey, newSlot);
            if (existingSlot != null) {
                if (!existingSlot.matches(input)) {
                    throw new IllegalStateException("Idempotency key was used for a different booking request");
                }
                return existingSlot.awaitResult();
            }

            try {
                Reservation reservation = bookReservation(input.getShowtimeId(), input.getSeatIds());
                newSlot.completeSuccess(reservation);
                return reservation;
            } catch (RuntimeException | Error exception) {
                newSlot.completeFailure(exception);
                bookingsByIdempotencyKey.remove(idempotencyKey, newSlot);
                throw exception;
            }
        }

        public void cancelReservation(String bookingId) {
            if (bookingId == null || bookingId.isEmpty()) {
                throw new IllegalArgumentException("Invalid confirmation ID");
            }

            Reservation reservation = reservationsById.get(bookingId);
            if (reservation == null) {
                throw new NoSuchElementException("Reservation not found: " + bookingId);
            }

            Showtime showtime = reservation.getShowtime();
            showtime.cancel(reservation);

            reservationsById.remove(bookingId);
        }

        public Reservation getReservation(String bookingId) {
            if (bookingId == null || bookingId.isEmpty()) {
                throw new IllegalArgumentException("Invalid confirmation ID");
            }

            Reservation reservation = reservationsById.get(bookingId);
            if (reservation == null) {
                throw new NoSuchElementException("Reservation not found: " + bookingId);
            }
            return reservation;
        }
    }


