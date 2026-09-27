package bookings.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRecordRepository extends JpaRepository<BookingRecord, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select booking from BookingRecord booking where booking.bookingId = :bookingId")
    Optional<BookingRecord> lockByBookingId(@Param("bookingId") String bookingId);
}
