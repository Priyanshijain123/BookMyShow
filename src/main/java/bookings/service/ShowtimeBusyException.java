package bookings.service;

public class ShowtimeBusyException extends RuntimeException {
    public ShowtimeBusyException(String message) {
        super(message);
    }
}
