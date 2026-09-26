# Cloud services (simple design)

- Run the Spring Boot app on a managed container service behind an HTTPS load balancer.
- Store theaters, showtimes, bookings, and idempotency keys in a managed relational database.
- Keep payment credentials in a secrets manager. Use a payment provider for real transactions.

The current project keeps bookings in memory and uses a lock inside each app instance. Before running multiple cloud instances, use database transactions and a unique constraint on `(showtime_id, seat_id)` to prevent double booking across instances.

## Optional SQS booking events

Set `BOOKING_SQS_QUEUE_URL` to an SQS queue URL and configure `AWS_REGION` and AWS credentials (or an IAM role). Each new booking attempts to send a `BOOKING_CREATED` JSON message with `bookingId`, `showtimeId`, and `seatIds`. Without a queue URL, bookings work locally without AWS. A failed SQS send is logged; the booking remains created. This simple demo does not retry missed messages. Consumers should use `bookingId` to ignore duplicate deliveries.
