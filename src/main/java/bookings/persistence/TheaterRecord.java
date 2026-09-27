package bookings.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "theater_catalog")
public class TheaterRecord {
    @Id
    @Column(name = "theater_id", nullable = false, length = 36)
    private String theaterId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    protected TheaterRecord() {
    }

    public TheaterRecord(String theaterId, String name, String city) {
        this.theaterId = theaterId;
        this.name = name;
        this.city = city;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }
}
