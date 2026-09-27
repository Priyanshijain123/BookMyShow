package bookings.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_records")
public class BookingRecord {
    @Id
    @Column(name = "booking_id", nullable = false, length = 36)
    private String bookingId;

    @Column(name = "showtime_id", nullable = false, length = 100)
    private String showtimeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingState status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_seats", joinColumns = @JoinColumn(name = "booking_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_booking_seat", columnNames = {"booking_id", "seat_id"}))
    @Column(name = "seat_id", nullable = false, length = 10)
    @OrderColumn(name = "seat_order")
    private List<String> seatIds = new ArrayList<>();

    protected BookingRecord() {
    }

    public BookingRecord(String bookingId, String showtimeId, List<String> seatIds) {
        this.bookingId = bookingId;
        this.showtimeId = showtimeId;
        this.seatIds = new ArrayList<>(seatIds);
        this.status = BookingState.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public BookingState getStatus() {
        return status;
    }

    public List<String> getSeatIds() {
        return new ArrayList<>(seatIds);
    }

    public void cancel() {
        this.status = BookingState.CANCELLED;
    }
}
