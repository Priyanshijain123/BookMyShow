package bookings.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BookingInput {
    private String showtimeId;
    private List<String> seatIds;

    public BookingInput(String showtimeId, List<String> seatIds) {
        this.showtimeId = Objects.requireNonNull(showtimeId, "showtimeId");
        this.seatIds = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(seatIds, "seatIds")));
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BookingInput input)) {
            return false;
        }
        return showtimeId.equals(input.showtimeId) && seatIds.equals(input.seatIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(showtimeId, seatIds);
    }
}
