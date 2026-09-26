package bookings.model;

public class Movie {
    private final String id;
    private final String title;
    private final String language;

    public Movie(String id, String title) {
        this(id, title, "Unknown");
    }

    public Movie(String id, String title, String language) {
        this.id = id;
        this.title = title;
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }
}
