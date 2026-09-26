package bookings.service;
import bookings.model.Movie;
import bookings.model.Reservation;
import bookings.model.Theater;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Showtime {

        private final String id;
        private final Theater theater;
        private final LocalDateTime datetime;
        private final String screenLabel;
        private final Movie movie;
        private final List<Reservation> reservations;
        private final Set<String> bookedSeatIds;
        private final ReentrantLock seatLock;

        public Showtime(String id, Theater theater, Movie movie, LocalDateTime datetime, String screenLabel) {
            this.id = id;
            this.theater = theater;
            this.movie = movie;
            this.datetime = datetime;
            this.screenLabel = screenLabel;
            this.reservations = new ArrayList<>();
            this.bookedSeatIds = new HashSet<>();
            this.seatLock = new ReentrantLock();
        }

        public String getId() {
            return id;
        }

        public Theater getTheater() {
            return theater;
        }

        public LocalDateTime getDatetime() {
            return datetime;
        }

        public Movie getMovie() {
            return movie;
        }



        public void book(Reservation reservation) {
            List<String> seatIds = reservation.getSeatIds();

            if (seatIds == null || seatIds.isEmpty()) {
                throw new IllegalArgumentException("Must select at least one seat");
            }

            Set<String> requestedSeats = new HashSet<>();
            for (String seatId : seatIds) {
                if (!isValidSeatId(seatId)) {
                    throw new IllegalArgumentException("Invalid seat: " + seatId);
                }
                if (!requestedSeats.add(seatId)) {
                    throw new IllegalArgumentException("Duplicate seat: " + seatId);
                }
            }

            boolean locked;
            try {
                locked = seatLock.tryLock(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ShowtimeBusyException("Interrupted while waiting to book showtime " + id);
            }
            if (!locked) {
                throw new ShowtimeBusyException("Showtime " + id + " is busy; retry the booking");
            }

            try {
                for (String seatId : seatIds) {
                    if (bookedSeatIds.contains(seatId)) {
                        throw new IllegalStateException("Seat " + seatId + " is not available");
                    }
                }

                reservations.add(reservation);
                bookedSeatIds.addAll(seatIds);
            } finally {
                seatLock.unlock();
            }
        }

        public void cancel(Reservation reservation) {
            seatLock.lock();
            try {
                if (reservations.remove(reservation)) {
                    bookedSeatIds.removeAll(reservation.getSeatIds());
                }
            } finally {
                seatLock.unlock();
            }
        }

        private boolean isValidSeatId(String seatId) {
            if (seatId == null || seatId.length() < 2) {
                return false;
            }
            char row = seatId.charAt(0);
            try {
                int num = Integer.parseInt(seatId.substring(1));
                return row >= 'A' && row <= 'Z' && num >= 0 && num <= 20;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }


