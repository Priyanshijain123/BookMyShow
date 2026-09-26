# BookMyShow booking API

A small Spring Boot application for finding movies and theaters, reserving seats, quoting discounted ticket prices, and simulating card or UPI payments. It starts with sample theaters in Mumbai and Delhi. Data is held in memory, so bookings and payments disappear when the application stops.

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
curl.exe "http://localhost:8080/v1/theaters/theater-1/showtimes"
```

Movie search filters are optional; theater search requires `city`. Search returns future showtimes. Sample IDs are `theater-1` / `show-1` (Mumbai) and `theater-2` / `show-2` (Delhi).

Create a booking with a unique `Idempotency-Key`:

```powershell
curl.exe -X POST "http://localhost:8080/v1/bookings" -H "Content-Type: application/json" -H "Idempotency-Key: demo-booking-1" -d '{"showtimeId":"show-1","seatIds":["A1","A2","A3"]}'
```

Copy the returned `bookingId` into the next URLs. Reusing the same key with the same request returns the original booking; using it with different seats is a conflict.

```powershell
curl.exe "http://localhost:8080/v1/bookings/BOOKING_ID"
curl.exe "http://localhost:8080/v1/bookings/BOOKING_ID/quote"
```

Use the quote's `totalAmountMinor` as the payment amount. For three seats in the sample 14:00 Mumbai show, the total is `40000` paise (₹400):

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
| `booking.discount.eligible-theaters` | `theater-1` | Theater IDs eligible for discounts |
| `booking.sqs.queue-url` | empty | Optional SQS queue for booking events |

Both the city and theater must be eligible for discounts. Every third ticket is 50% off; afternoon shows starting from 12:00 through 16:59 get a further 20% off. See [discount rules](docs/discounts.md).

For local use, leave the SQS URL empty. To enable it, set the queue URL and configure AWS region and credentials for the AWS SDK. Booking still succeeds if publishing fails; the failure is logged.

## Design documents

- High-level design: [PNG](docs/hld.png) · [explanation](docs/hld.md)
- Booking sequence: [PNG](docs/sequence.png) · [all sequence flows](docs/sequence.md)
- UML class diagram: [PNG](docs/uml.png) · [class details](docs/uml.md)
- [Proposed database schema](docs/schema.md) — design only, not implemented
- [Cloud deployment notes](docs/cloud.md)

Seat conflicts are prevented within this process by a `ReentrantLock` on each showtime. The application has no database and is not safe to scale to multiple instances for shared bookings without a database seat-uniqueness constraint.

The PNGs can be regenerated with `python docs/render_diagrams.py` after installing Pillow.
