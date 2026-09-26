# Ticket discounts

The sample ticket price is ₹200 (`booking.ticket-price-minor=20000`). Discounts apply only when **both** the theater city and theater ID are selected. The defaults are Mumbai and `theater-1`. Configure other locations with comma-separated `booking.discount.eligible-cities` and `booking.discount.eligible-theaters` values.

- Every third ticket in one booking is 50% off.
- Shows starting from 12:00 through 16:59 receive 20% off each ticket.
- When both apply, the afternoon discount is calculated after the third-ticket discount. For three ₹200 tickets at 14:00, the total is ₹400 (base ₹600, discount ₹200).

After creating a booking, call `GET /v1/bookings/{bookingId}/quote`. Send its `totalAmountMinor` as `amountMinor` when calling `POST /v1/bookings/{bookingId}/payments`. Payments with another amount are rejected.
