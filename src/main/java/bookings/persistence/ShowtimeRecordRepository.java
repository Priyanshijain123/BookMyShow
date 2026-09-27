package bookings.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowtimeRecordRepository extends JpaRepository<ShowtimeRecord, String> {
    List<ShowtimeRecord> findByTheaterIdOrderByStartsAt(String theaterId);
}
