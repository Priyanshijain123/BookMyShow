package bookings.model;

import bookings.service.Showtime;
import java.time.LocalDateTime;

public class ShowtimeResponse {
    private final String showtimeId;
    private final String movieId;
    private final String movieTitle;
    private final String language;
    private final LocalDateTime datetime;
    private final String screenLabel;

    public ShowtimeResponse(String showtimeId, String movieId, String movieTitle, String language,
                            LocalDateTime datetime, String screenLabel) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.language = language;
        this.datetime = datetime;
        this.screenLabel = screenLabel;
    }

    public String getShowtimeId() { return showtimeId; }
    public String getMovieId() { return movieId; }
    public String getMovieTitle() { return movieTitle; }
    public String getLanguage() { return language; }
    public LocalDateTime getDatetime() { return datetime; }
    public String getScreenLabel() { return screenLabel; }

    public static ShowtimeResponse from(Showtime showtime) {
        return new ShowtimeResponse(showtime.getId(), showtime.getMovie().getId(),
                showtime.getMovie().getTitle(), showtime.getMovie().getLanguage(),
                showtime.getDatetime(), showtime.getScreenLabel());
    }
}
