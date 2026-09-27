package bookings.model;

import java.time.LocalDateTime;

public class ManageShowtimeRequest {
    private String movieId;
    private String movieTitle;
    private String language;
    private LocalDateTime datetime;
    private String screenLabel;

    public ManageShowtimeRequest() {
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public String getScreenLabel() {
        return screenLabel;
    }

    public void setScreenLabel(String screenLabel) {
        this.screenLabel = screenLabel;
    }
}
