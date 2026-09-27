package bookings.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TheaterRecordRepository extends JpaRepository<TheaterRecord, String> {
}
