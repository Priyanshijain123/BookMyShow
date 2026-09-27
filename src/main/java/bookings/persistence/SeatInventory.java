package bookings.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "seat_inventory", uniqueConstraints =
        @UniqueConstraint(name = "uk_seat_inventory_showtime_seat", columnNames = {"showtime_id", "seat_id"}))
public class SeatInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "showtime_id", nullable = false, length = 100)
    private String showtimeId;

    @Column(name = "seat_id", nullable = false, length = 10)
    private String seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatState status;

    @Column(name = "booking_id", length = 36)
    private String bookingId;

    protected SeatInventory() {
    }

    public SeatInventory(String showtimeId, String seatId) {
        this.showtimeId = showtimeId;
        this.seatId = seatId;
        this.status = SeatState.AVAILABLE;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public String getSeatId() {
        return seatId;
    }

    public SeatState getStatus() {
        return status;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void book(String bookingId) {
        this.status = SeatState.BOOKED;
        this.bookingId = bookingId;
    }

    public void release() {
        this.status = SeatState.AVAILABLE;
        this.bookingId = null;
    }
}
