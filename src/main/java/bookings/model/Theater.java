package bookings.model;
import bookings.service.Showtime;

import java.util.ArrayList;
import java.util.List;

public class Theater {

        private final String id;
        private final String name;
        private final String city;
        private final List<Showtime> showtimes;

        public Theater(String id, String name) {
            this(id, name, "Unknown");
        }

        public Theater(String id, String name, String city) {
            this.id = id;
            this.name = name;
            this.city = city;
            this.showtimes = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCity() {
            return city;
        }

        public List<Showtime> getShowtimes() {
            return showtimes;
        }

        public List<Showtime> getShowtimesForMovie(Movie movie) {
            List<Showtime> results = new ArrayList<>();
            for (Showtime showtime : showtimes) {
                if (showtime.getMovie().getId().equals(movie.getId())) {
                    results.add(showtime);
                }
            }
            return results;
        }
    }


