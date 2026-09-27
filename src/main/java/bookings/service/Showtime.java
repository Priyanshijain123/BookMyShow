package bookings.service;
import bookings.model.Movie;
import bookings.model.Theater;

import java.time.LocalDateTime;

public class Showtime {

        private final String id;
        private final Theater theater;
        private final LocalDateTime datetime;
        private final String screenLabel;
        private final Movie movie;

        public Showtime(String id, Theater theater, Movie movie, LocalDateTime datetime, String screenLabel) {
            this.id = id;
            this.theater = theater;
            this.movie = movie;
            this.datetime = datetime;
            this.screenLabel = screenLabel;
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

        public String getScreenLabel() {
            return screenLabel;
        }

    }
