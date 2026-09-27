package bookings.controller;

import bookings.model.CreateTheaterRequest;
import bookings.model.ManageShowtimeRequest;
import bookings.model.ShowtimeResponse;
import bookings.model.TheaterSearchResult;
import bookings.service.BookingSystem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping
    public List<TheaterSearchResult> getTheaters() {
        return bookingSystem.getTheaters().stream()
                .map(TheaterSearchResult::from)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<TheaterSearchResult> createTheater(@RequestBody CreateTheaterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Theater details are required");
        }
        TheaterSearchResult response = TheaterSearchResult.from(
                bookingSystem.createTheater(request.getName(), request.getCity()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @PostMapping("/{theaterId}/showtimes")
    public ResponseEntity<ShowtimeResponse> createShowtime(@PathVariable("theaterId") String theaterId,
                                                            @RequestBody ManageShowtimeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Showtime details are required");
        }
        ShowtimeResponse response = ShowtimeResponse.from(bookingSystem.createShowtime(theaterId,
                request.getMovieId(), request.getMovieTitle(), request.getLanguage(),
                request.getDatetime(), request.getScreenLabel()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
