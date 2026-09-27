package bookings.service;
import bookings.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingSystem {
        private static final Logger LOGGER = Logger.getLogger(BookingSystem.class.getName());

        private final List<Theater> theaters;
        private final BookingEventPublisher bookingEventPublisher;
        private final SeatBookingService seatBookingService;
        private final TheaterCatalogService theaterCatalogService;
        private final Map<String, Showtime> showtimesById;
        private final Map<String, IdempotencySlot> bookingsByIdempotencyKey;

        public BookingSystem(BookingEventPublisher bookingEventPublisher, SeatBookingService seatBookingService,
                             TheaterCatalogService theaterCatalogService) {
            this.theaters = new ArrayList<>();
            this.bookingEventPublisher = Objects.requireNonNull(bookingEventPublisher);
            this.seatBookingService = Objects.requireNonNull(seatBookingService);
            this.theaterCatalogService = Objects.requireNonNull(theaterCatalogService);
            this.showtimesById = new ConcurrentHashMap<>();
            this.bookingsByIdempotencyKey = new ConcurrentHashMap<>();
        }

        public synchronized void initializeCatalog() {
            if (!theaters.isEmpty()) {
                return;
            }
            List<Theater> persistedTheaters = theaterCatalogService.loadCatalog();
            theaters.addAll(persistedTheaters);
            for (Theater theater : persistedTheaters) {
                for (Showtime showtime : theater.getShowtimes()) {
                    showtimesById.put(showtime.getId(), showtime);
                }
            }
            seatBookingService.initializeInventory(persistedTheaters);
        }


        public synchronized List<Showtime> searchMovies(String title, String language, String city) {
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

        public synchronized List<Theater> searchTheaters(String city) {
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

        public synchronized List<Theater> getTheaters() {
            return new ArrayList<>(theaters);
        }

        private String normalize(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        }

        public synchronized List<Showtime> getShowtimesAtTheater(Theater theater) {
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

        public synchronized List<Showtime> getShowtimesAtTheater(String theaterId) {
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

            Reservation reservation = seatBookingService.book(showtime, seatIds);

            try {
                bookingEventPublisher.publish(reservation);
            } catch (RuntimeException exception) {
                LOGGER.log(Level.WARNING, "Booking created, but SQS notification failed", exception);
            }

            return reservation;
        }

        public synchronized Showtime createShowtime(String theaterId, String movieId, String movieTitle,
                                                     String language, LocalDateTime datetime, String screenLabel) {
            Theater theater = findTheater(theaterId);
            validateShowtimeDetails(movieId, movieTitle, language, datetime, screenLabel);
            ensureNoScheduleConflict(theater, datetime, screenLabel);

            Showtime showtime = new Showtime(UUID.randomUUID().toString(), theater,
                    new Movie(movieId.trim(), movieTitle.trim(), language.trim()), datetime,
                    screenLabel.trim());
            theaterCatalogService.createShowtime(showtime);
            theater.getShowtimes().add(showtime);
            showtimesById.put(showtime.getId(), showtime);
            return showtime;
        }

        public synchronized Theater createTheater(String name, String city) {
            Theater theater = theaterCatalogService.createTheater(name, city);
            theaters.add(theater);
            return theater;
        }

        private Theater findTheater(String theaterId) {
            if (theaterId == null || theaterId.isBlank()) {
                throw new IllegalArgumentException("Theater ID is required");
            }
            return theaters.stream()
                    .filter(theater -> theater.getId().equals(theaterId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Theater not found: " + theaterId));
        }

        private void validateShowtimeDetails(String movieId, String movieTitle, String language,
                                             LocalDateTime datetime, String screenLabel) {
            if (movieId == null || movieId.isBlank() || movieTitle == null || movieTitle.isBlank()
                    || language == null || language.isBlank() || screenLabel == null || screenLabel.isBlank()
                    || datetime == null) {
                throw new IllegalArgumentException("Movie details, showtime, and screen are required");
            }
            if (!datetime.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Showtime must be in the future");
            }
        }

        private void ensureNoScheduleConflict(Theater theater, LocalDateTime datetime, String screenLabel) {
            boolean conflict = theater.getShowtimes().stream()
                    .anyMatch(showtime -> showtime.getDatetime().equals(datetime)
                            && showtime.getScreenLabel().equalsIgnoreCase(screenLabel.trim()));
            if (conflict) {
                throw new IllegalStateException("A show is already scheduled on this screen at that time");
            }
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

            seatBookingService.cancel(bookingId);
        }

        public Reservation getReservation(String bookingId) {
            if (bookingId == null || bookingId.isEmpty()) {
                throw new IllegalArgumentException("Invalid confirmation ID");
            }

            return seatBookingService.getReservation(bookingId, showtimesById);
        }
    }
