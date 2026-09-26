package bookings.controller;

import bookings.model.MovieSearchResult;
import bookings.service.BookingSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/movies")
public class MovieController {
    private final BookingSystem bookingSystem;

    public MovieController(BookingSystem bookingSystem) {
        this.bookingSystem = bookingSystem;
    }

    @GetMapping("/search")
    public List<MovieSearchResult> search(@RequestParam(value = "title", required = false) String title,
                                          @RequestParam(value = "language", required = false) String language,
                                          @RequestParam(value = "city", required = false) String city) {
        return bookingSystem.searchMovies(title, language, city).stream()
                .map(MovieSearchResult::from)
                .collect(Collectors.toList());
    }
}
