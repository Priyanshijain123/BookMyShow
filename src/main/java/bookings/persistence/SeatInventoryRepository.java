package bookings.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {
    List<SeatInventory> findByShowtimeIdOrderBySeatId(String showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select seat from SeatInventory seat where seat.showtimeId = :showtimeId "
            + "and seat.seatId in :seatIds order by seat.seatId")
    List<SeatInventory> lockSeats(@Param("showtimeId") String showtimeId,
                                  @Param("seatIds") List<String> seatIds);
}
