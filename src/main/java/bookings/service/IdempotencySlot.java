package bookings.service;

import bookings.model.BookingInput;
import bookings.model.Reservation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class IdempotencySlot {
    private final BookingInput input;
    private final CompletableFuture<Reservation> result = new CompletableFuture<>();

    public IdempotencySlot(BookingInput input) {
        this.input = input;
    }

    public boolean matches(BookingInput other) {
        return input.equals(other);
    }

    public Reservation awaitResult() {
        try {
            return result.join();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw exception;
        }
    }

    public void completeSuccess(Reservation reservation) {
        result.complete(reservation);
    }

    public void completeFailure(Throwable exception) {
        result.completeExceptionally(exception);
    }
}
