package bookings.service;

import bookings.model.Reservation;
import bookings.model.Theater;
import bookings.persistence.BookingRecord;
import bookings.persistence.BookingRecordRepository;
import bookings.persistence.BookingState;
import bookings.persistence.SeatInventory;
import bookings.persistence.SeatInventoryRepository;
import bookings.persistence.SeatState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class SeatBookingService {
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRecordRepository bookingRecordRepository;

    public SeatBookingService(SeatInventoryRepository seatInventoryRepository,
                              BookingRecordRepository bookingRecordRepository) {
        this.seatInventoryRepository = seatInventoryRepository;
        this.bookingRecordRepository = bookingRecordRepository;
    }

    @Transactional
    public void initializeInventory(List<Theater> theaters) {
        for (Theater theater : theaters) {
            for (Showtime showtime : theater.getShowtimes()) {
                initializeInventory(showtime);
            }
        }
    }

    @Transactional
    public void initializeInventory(Showtime showtime) {
        Set<String> existingSeats = new HashSet<>();
        for (SeatInventory seat : seatInventoryRepository.findByShowtimeIdOrderBySeatId(showtime.getId())) {
            existingSeats.add(seat.getSeatId());
        }

        List<SeatInventory> newSeats = new ArrayList<>();
        for (char row = 'A'; row <= 'Z'; row++) {
            for (int number = 0; number <= 20; number++) {
                String seatId = row + Integer.toString(number);
                if (!existingSeats.contains(seatId)) {
                    newSeats.add(new SeatInventory(showtime.getId(), seatId));
                }
            }
        }
        seatInventoryRepository.saveAll(newSeats);
    }

    @Transactional
    public Reservation book(Showtime showtime, List<String> seatIds) {
        List<String> orderedSeatIds = validateAndOrderSeatIds(seatIds);
        List<SeatInventory> lockedSeats = seatInventoryRepository.lockSeats(showtime.getId(), orderedSeatIds);
        if (lockedSeats.size() != orderedSeatIds.size()) {
            throw new IllegalArgumentException("One or more requested seats do not exist");
        }
        if (lockedSeats.stream().anyMatch(seat -> seat.getStatus() != SeatState.AVAILABLE)) {
            throw new IllegalStateException("One or more seats are not available");
        }

        String bookingId = UUID.randomUUID().toString();
        bookingRecordRepository.save(new BookingRecord(bookingId, showtime.getId(), seatIds));
        lockedSeats.forEach(seat -> seat.book(bookingId));
        seatInventoryRepository.flush();
        return new Reservation(bookingId, showtime, seatIds);
    }

    @Transactional
    public void cancel(String bookingId) {
        BookingRecord booking = bookingRecordRepository.lockByBookingId(bookingId)
                .filter(record -> record.getStatus() == BookingState.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found: " + bookingId));

        List<String> seatIds = validateAndOrderSeatIds(booking.getSeatIds());
        List<SeatInventory> lockedSeats = seatInventoryRepository.lockSeats(booking.getShowtimeId(), seatIds);
        if (lockedSeats.size() != seatIds.size()
                || lockedSeats.stream().anyMatch(seat -> !bookingId.equals(seat.getBookingId()))) {
            throw new IllegalStateException("Booking seat inventory is inconsistent: " + bookingId);
        }

        lockedSeats.forEach(SeatInventory::release);
        booking.cancel();
        seatInventoryRepository.flush();
    }

    public Reservation getReservation(String bookingId, Map<String, Showtime> showtimesById) {
        BookingRecord booking = bookingRecordRepository.findById(bookingId)
                .filter(record -> record.getStatus() == BookingState.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found: " + bookingId));
        Showtime showtime = showtimesById.get(booking.getShowtimeId());
        if (showtime == null) {
            throw new NoSuchElementException("Showtime not found: " + booking.getShowtimeId());
        }
        return new Reservation(booking.getBookingId(), showtime, booking.getSeatIds());
    }

    private List<String> validateAndOrderSeatIds(List<String> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Must select at least one seat");
        }

        Set<String> uniqueSeatIds = new HashSet<>();
        for (String seatId : seatIds) {
            if (!isValidSeatId(seatId)) {
                throw new IllegalArgumentException("Invalid seat: " + seatId);
            }
            if (!uniqueSeatIds.add(seatId)) {
                throw new IllegalArgumentException("Duplicate seat: " + seatId);
            }
        }
        return uniqueSeatIds.stream().sorted().toList();
    }

    private boolean isValidSeatId(String seatId) {
        if (seatId == null || seatId.length() < 2) {
            return false;
        }
        char row = seatId.charAt(0);
        try {
            int number = Integer.parseInt(seatId.substring(1));
            return row >= 'A' && row <= 'Z' && number >= 0 && number <= 20;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
