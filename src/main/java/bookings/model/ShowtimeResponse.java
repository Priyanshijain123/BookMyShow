package bookings.model;

import bookings.service.Showtime;
import java.time.LocalDateTime;

public class ShowtimeResponse {
    private final String showtimeId;
    private final String movieId;
    private final String movieTitle;
    private final LocalDateTime datetime;

    public ShowtimeResponse(String showtimeId, String movieId, String movieTitle, LocalDateTime datetime) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.datetime = datetime;
    }

    public String getShowtimeId() { return showtimeId; }
    public String getMovieId() { return movieId; }
    public String getMovieTitle() { return movieTitle; }
    public LocalDateTime getDatetime() { return datetime; }

    public static ShowtimeResponse from(Showtime showtime) {
        return new ShowtimeResponse(showtime.getId(), showtime.getMovie().getId(),
                showtime.getMovie().getTitle(), showtime.getDatetime());
    }
}
