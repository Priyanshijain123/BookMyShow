package bookings.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "showtime_catalog",
        uniqueConstraints = @UniqueConstraint(name = "uk_showtime_screen_time",
                columnNames = {"theater_id", "screen_key", "starts_at"}))
public class ShowtimeRecord {
    @Id
    @Column(name = "showtime_id", nullable = false, length = 36)
    private String showtimeId;

    @Column(name = "theater_id", nullable = false, length = 36)
    private String theaterId;

    @Column(name = "movie_id", nullable = false, length = 100)
    private String movieId;

    @Column(name = "movie_title", nullable = false, length = 200)
    private String movieTitle;

    @Column(nullable = false, length = 100)
    private String language;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "screen_label", nullable = false, length = 100)
    private String screenLabel;

    @Column(name = "screen_key", nullable = false, length = 100)
    private String screenKey;

    protected ShowtimeRecord() {
    }

    public ShowtimeRecord(String showtimeId, String theaterId, String movieId, String movieTitle,
                          String language, LocalDateTime startsAt, String screenLabel) {
        this.showtimeId = showtimeId;
        this.theaterId = theaterId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.language = language;
        this.startsAt = startsAt;
        this.screenLabel = screenLabel;
        this.screenKey = screenLabel.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getLanguage() {
        return language;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public String getScreenLabel() {
        return screenLabel;
    }

}
