package bookings.controller;

import bookings.model.ShowtimeResponse;
import bookings.model.TheaterSearchResult;
import bookings.service.BookingSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/theaters")
public class TheaterController {
    private final BookingSystem bookingSystem;

    public TheaterController(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
    }

    @GetMapping("/search")
    public List<TheaterSearchResult> search(@RequestParam("city") String city) {
        return bookingSystem.searchTheaters(city).stream()
                .map(TheaterSearchResult::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{theaterId}/showtimes")
    public List<ShowtimeResponse> getShowtimesAtTheater(@PathVariable("theaterId") String theaterId) {
        return bookingSystem.getShowtimesAtTheater(theaterId).stream()
                .map(ShowtimeResponse::from)
                .collect(Collectors.toList());
    }
}
