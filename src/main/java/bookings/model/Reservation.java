package bookings.model;

import bookings.service.Showtime;

import java.util.ArrayList;
import java.util.List;

public class Reservation {
    private final String bookingId;
    private final Showtime showtime;

    public String getBookingId() {
        return bookingId;
    }

    private final List<String> seatIds;

    public Reservation(String bookingId, Showtime showtime, List<String> seatIds) {
        this.bookingId = bookingId;
        this.showtime = showtime;
        this.seatIds = new ArrayList<>(seatIds);
    }


    public Showtime getShowtime() {
        return showtime;
    }

    public List<String> getSeatIds() {
        return new ArrayList<>(seatIds);
    }
}
