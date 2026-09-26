package bookings.model;

import java.util.List;

public class BookingResponse {
        private String bookingId;
        private String showtimeId;
        private List<String> seatIds;

        public BookingResponse() {
        }

        public BookingResponse(String bookingId, String showtimeId, List<String> seatIds) {
            this.bookingId = bookingId;
            this.showtimeId = showtimeId;
            this.seatIds = seatIds;
        }

        public String getBookingId() {
            return bookingId;
        }

        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }

        public String getShowtimeId() {
            return showtimeId;
        }

        public void setShowtimeId(String showtimeId) {
            this.showtimeId = showtimeId;
        }

        public List<String> getSeatIds() {
            return seatIds;
        }

        public void setSeatIds(List<String> seatIds) {
            this.seatIds = seatIds;
        }

        public static BookingResponse from(Reservation reservation) {
            return new BookingResponse(reservation.getBookingId(),
                    reservation.getShowtime().getId(), reservation.getSeatIds());
        }
    }
