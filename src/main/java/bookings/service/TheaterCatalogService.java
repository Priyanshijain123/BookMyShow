package bookings.service;

import bookings.model.Movie;
import bookings.model.Theater;
import bookings.persistence.ShowtimeRecord;
import bookings.persistence.ShowtimeRecordRepository;
import bookings.persistence.TheaterRecord;
import bookings.persistence.TheaterRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TheaterCatalogService {
    private final TheaterRecordRepository theaterRepository;
    private final ShowtimeRecordRepository showtimeRepository;
    private final SeatBookingService seatBookingService;

    public TheaterCatalogService(TheaterRecordRepository theaterRepository,
                                 ShowtimeRecordRepository showtimeRepository,
                                 SeatBookingService seatBookingService) {
        this.theaterRepository = theaterRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatBookingService = seatBookingService;
    }

    @Transactional(readOnly = true)
    public List<Theater> loadCatalog() {
        List<Theater> theaters = new ArrayList<>();
        for (TheaterRecord theaterRecord : theaterRepository.findAll()) {
            Theater theater = new Theater(theaterRecord.getTheaterId(),
                    theaterRecord.getName(), theaterRecord.getCity());
            for (ShowtimeRecord record :
                    showtimeRepository.findByTheaterIdOrderByStartsAt(theater.getId())) {
                theater.getShowtimes().add(new Showtime(record.getShowtimeId(), theater,
                        new Movie(record.getMovieId(), record.getMovieTitle(), record.getLanguage()),
                        record.getStartsAt(), record.getScreenLabel()));
            }
            theaters.add(theater);
        }
        return theaters;
    }


    public Theater createTheater(String name, String city) {
        if (name == null || name.isBlank() || city == null || city.isBlank()) {
            throw new IllegalArgumentException("Theater name and city are required");
        }
        Theater theater = new Theater(UUID.randomUUID().toString(), name.trim(), city.trim());
        theaterRepository.save(new TheaterRecord(theater.getId(), theater.getName(), theater.getCity()));
        return theater;
    }

    @Transactional
    public void createShowtime(Showtime showtime) {
        showtimeRepository.save(toRecord(showtime));
        seatBookingService.initializeInventory(showtime);
    }

    private ShowtimeRecord toRecord(Showtime showtime) {
        return new ShowtimeRecord(showtime.getId(), showtime.getTheater().getId(),
                showtime.getMovie().getId(), showtime.getMovie().getTitle(),
                showtime.getMovie().getLanguage(), showtime.getDatetime(), showtime.getScreenLabel());
    }
}
