# BookMyShow booking API

A small Spring Boot application for managing theaters and shows, reserving seats, quoting discounted ticket prices, and simulating card or UPI payments. Theater/show catalogs, seat inventory, and bookings are persisted in a database; simulated payments and idempotency keys remain in memory.

## Requirements

- JDK **25** (the Maven project targets Java 25)
- Maven 3.9+ or IntelliJ IDEA's bundled Maven
- Port **8080** available

Check the SDK used by your terminal:

```powershell
java -version
mvn -version
```

In IntelliJ, set **Project SDK** and **Module SDK** to JDK 25 under **File → Project Structure**, and use JDK 25 for the `BookingApplication` run configuration. If `mvn` is not on your PATH, run the Maven lifecycle goals from IntelliJ's Maven tool window.

## Build and start

Open PowerShell in this project directory and run:

```powershell
mvn clean package
java -jar target/BookMyShow-1.0-SNAPSHOT.jar
```

On this Windows machine, the installed JDK can be selected for the current PowerShell session with:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

You can also run `bookings.BookingApplication` from IntelliJ. Startup succeeds when the log says **Tomcat started on port 8080**. Stop the app with **Ctrl+C** in PowerShell or the **Stop** button in IntelliJ. The root URL `/` has no page; use the API endpoints below.

## Try the API

Use `curl.exe` in PowerShell. The same methods, URLs, headers, and JSON bodies work in Postman.

```powershell
curl.exe "http://localhost:8080/v1/movies/search?city=Mumbai&language=Hindi&title=Sample"
curl.exe "http://localhost:8080/v1/theaters/search?city=Mumbai"
curl.exe "http://localhost:8080/v1/theaters"
```

Movie search filters are optional; theater search requires `city`. Search returns future showtimes. The application starts with an empty theater catalog.

Create a theater and save the returned `theaterId`:

```powershell
curl.exe -X POST "http://localhost:8080/v1/theaters" -H "Content-Type: application/json" -d '{"name":"City Cinema","city":"Mumbai"}'
```

Theater staff can then add future shows. The request body includes all show details:

```powershell
$theaterId = 'THEATER_ID_FROM_CREATE'
$show = '{"movieId":"movie-3","movieTitle":"New Movie","language":"Hindi","datetime":"2027-01-15T18:30:00","screenLabel":"Screen 2"}'
curl.exe -X POST "http://localhost:8080/v1/theaters/$theaterId/showtimes" -H "Content-Type: application/json" -d $show
```

Showtimes must be in the future; a screen cannot have two shows at the same time. Theater and show records persist across restarts. The project has no staff authentication yet, so do not expose these management endpoints publicly.

Create a booking using the created `showtimeId` and a unique `Idempotency-Key`:

```powershell
curl.exe -X POST "http://localhost:8080/v1/bookings" -H "Content-Type: application/json" -H "Idempotency-Key: demo-booking-1" -d '{"showtimeId":"SHOWTIME_ID","seatIds":["A1","A2","A3"]}'
```

Copy the returned `bookingId` into the next URLs. Reusing the same key with the same request returns the original booking; using it with different seats is a conflict.

```powershell
curl.exe "http://localhost:8080/v1/bookings/BOOKING_ID"
curl.exe "http://localhost:8080/v1/bookings/BOOKING_ID/quote"
```

Use the quote's `totalAmountMinor` as the payment amount:

```powershell
curl.exe -X POST "http://localhost:8080/v1/bookings/BOOKING_ID/payments" -H "Content-Type: application/json" -d '{"method":"CARD","amountMinor":40000}'
curl.exe "http://localhost:8080/v1/bookings/BOOKING_ID/payments"
```

To cancel an **unpaid** booking and release its seats:

```powershell
curl.exe -X DELETE "http://localhost:8080/v1/bookings/BOOKING_ID"
```

Card and UPI payments are simulated. A paid booking cannot be canceled because the project has no refund flow.

## Configuration

Settings are in [`src/main/resources/application.properties`](src/main/resources/application.properties):

| Property | Default | Purpose |
| --- | --- | --- |
| `server.port` | `8080` | HTTP port |
| `booking.ticket-price-minor` | `20000` | Price per ticket in paise |
| `booking.discount.eligible-cities` | `Mumbai` | Cities eligible for discounts |
| `booking.discount.eligible-theaters` | empty | Comma-separated theater IDs eligible for discounts |
| `booking.sqs.queue-url` | empty | Optional SQS queue for booking events |

The default database is a persistent local H2 file at `./data/bookmyshow`. Configure `BOOKING_DB_URL`, `BOOKING_DB_USERNAME`, and `BOOKING_DB_PASSWORD` to connect to a shared database such as PostgreSQL. Theater/show catalogs, bookings, and seat inventory persist in the database. Booking and cancellation use JPA pessimistic row locks on the affected seats, held for the duration of the `@Transactional` operation. The database also enforces uniqueness for each `(showtime_id, seat_id)` inventory row.

Both the city and theater must be eligible for discounts. Every third ticket is 50% off; afternoon shows starting from 12:00 through 16:59 get a further 20% off. See [discount rules](docs/discounts.md).

For local use, leave the SQS URL empty. To enable it, set the queue URL and configure AWS region and credentials for the AWS SDK. Booking still succeeds if publishing fails; the failure is logged.

## Design documents

- High-level design: [PNG](docs/hld.png) · [explanation](docs/hld.md)
- Booking sequence: [PNG](docs/sequence.png) · [all sequence flows](docs/sequence.md)
- UML class diagram: [PNG](docs/uml.png) · [class details](docs/uml.md)
- [Database schema](docs/schema.md) — implemented catalog and seat/booking tables, plus future schema design
- [Cloud deployment notes](docs/cloud.md)

The database persists theater and showtime catalog data. Simulated payment records and idempotency-key state remain in memory.

The PNGs can be regenerated with `python docs/render_diagrams.py` after installing Pillow.
