# UML class diagram

![Current UML class diagram](uml.png)

The diagram focuses on the classes that own booking, seat, pricing, and payment behavior. Request and response DTOs are omitted for readability.

```mermaid
classDiagram
    class Movie {
        +String id
        +String title
        +String language
    }
    class Theater {
        +String id
        +String name
        +String city
        +List~Showtime~ showtimes
    }
    class Showtime {
        +String id
        +LocalDateTime datetime
        -Set~String~ bookedSeatIds
        -ReentrantLock seatLock
        +book(reservation)
        +cancel(reservation)
    }
    class Reservation {
        +String bookingId
        +List~String~ seatIds
    }
    class BookingSystem {
        -Map reservationsById
        -Map bookingsByIdempotencyKey
        +searchMovies(title, language, city)
        +book(showtimeId, seatIds, key)
        +cancelReservation(bookingId)
    }
    class IdempotencySlot {
        +matches(input)
        +awaitResult()
        +completeSuccess(reservation)
    }
    class PaymentService {
        -Map paymentsByBookingId
        +quote(bookingId)
        +pay(bookingId, method, amountMinor)
    }
    class TicketPricingService {
        +quote(reservation) PriceQuote
    }
    class DiscountPolicy {
        <<interface>>
        +discount(ticketNumber, showHour, currentPrice)
    }
    class PaymentGateway {
        <<interface>>
        +method() PaymentMethod
        +charge(paymentId, amountMinor, currency)
    }
    class BookingEventPublisher {
        <<interface>>
        +publish(reservation)
    }
    class SimulatedCardGateway
    class SimulatedUpiGateway
    class SqsBookingEventPublisher
    Theater "1" o-- "many" Showtime
    Showtime --> Movie
    Showtime "1" o-- "many" Reservation
    BookingSystem --> Showtime
    BookingSystem --> IdempotencySlot
    BookingSystem --> BookingEventPublisher
    PaymentService --> BookingSystem
    PaymentService --> TicketPricingService
    PaymentService --> PaymentGateway
    TicketPricingService --> DiscountPolicy
    PaymentGateway <|.. SimulatedCardGateway
    PaymentGateway <|.. SimulatedUpiGateway
    BookingEventPublisher <|.. SqsBookingEventPublisher
```

`PaymentGateway` and `DiscountPolicy` use the Strategy pattern. `SqsBookingEventPublisher` is used only when a queue URL is configured.
