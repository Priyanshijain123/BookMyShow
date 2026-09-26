package bookings.model;

import bookings.service.Showtime;
import java.time.LocalDateTime;

public class MovieSearchResult {
    private final String movieId;
    private final String title;
    private final String language;
    private final String showtimeId;
    private final LocalDateTime datetime;
    private final String theaterId;
    private final String theaterName;
    private final String city;

    public MovieSearchResult(String movieId, String title, String language, String showtimeId,
                             LocalDateTime datetime, String theaterId, String theaterName, String city) {
        this.movieId = movieId;
        this.title = title;
        this.language = language;
        this.showtimeId = showtimeId;
        this.datetime = datetime;
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.city = city;
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getLanguage() { return language; }
    public String getShowtimeId() { return showtimeId; }
    public LocalDateTime getDatetime() { return datetime; }
    public String getTheaterId() { return theaterId; }
    public String getTheaterName() { return theaterName; }
    public String getCity() { return city; }

    public static MovieSearchResult from(Showtime showtime) {
        return new MovieSearchResult(showtime.getMovie().getId(), showtime.getMovie().getTitle(),
                showtime.getMovie().getLanguage(), showtime.getId(), showtime.getDatetime(),
                showtime.getTheater().getId(), showtime.getTheater().getName(),
                showtime.getTheater().getCity());
    }
}
