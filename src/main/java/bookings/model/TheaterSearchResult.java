package bookings.model;

public class TheaterSearchResult {
    private final String theaterId;
    private final String theaterName;
    private final String city;

    public TheaterSearchResult(String theaterId, String theaterName, String city) {
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.city = city;
    }

    public String getTheaterId() { return theaterId; }
    public String getTheaterName() { return theaterName; }
    public String getCity() { return city; }

    public static TheaterSearchResult from(Theater theater) {
        return new TheaterSearchResult(theater.getId(), theater.getName(), theater.getCity());
    }
}
