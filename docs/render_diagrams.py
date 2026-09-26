"""Render the three PNG overview diagrams. Requires Pillow."""

from pathlib import Path
import math

from PIL import Image, ImageDraw, ImageFont


OUT = Path(__file__).resolve().parent
FONT_DIR = Path(r"C:\Windows\Fonts")
REGULAR = FONT_DIR / "segoeui.ttf"
BOLD = FONT_DIR / "segoeuib.ttf"
MONO = FONT_DIR / "consola.ttf"

INK = "#17253b"
MUTED = "#53647d"
LINE = "#b8c7dc"
NAVY = "#193d69"
TEAL = "#0d7c85"
PURPLE = "#7251ad"
AMBER = "#a76a10"
BLUE_BG = "#edf5ff"
TEAL_BG = "#eaf8f7"
PURPLE_BG = "#f3efff"
AMBER_BG = "#fff7e6"


def font(size, bold=False, mono=False):
    return ImageFont.truetype(str(MONO if mono else BOLD if bold else REGULAR), size)


def new_canvas(width, height, title, subtitle):
    image = Image.new("RGB", (width, height), "#f8fbff")
    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle((28, 28, width - 28, height - 28), radius=32,
                           fill="white", outline="#d7e3f1", width=3)
    draw.rounded_rectangle((28, 28, width - 28, 168), radius=32, fill=NAVY)
    draw.rectangle((28, 105, width - 28, 168), fill=NAVY)
    draw.text((78, 50), title, font=font(56, bold=True), fill="white")
    draw.text((80, 117), subtitle, font=font(25), fill="#dceafa")
    return image, draw


def text_lines(draw, lines, x, y, size=27, spacing=41, color=INK, bold=False):
    for line in lines:
        draw.text((x, y), line, font=font(size, bold=bold), fill=color)
        y += spacing


def card(draw, box, title, lines=(), fill=BLUE_BG, outline=LINE,
         title_size=30, body_size=25, line_spacing=39):
    x1, y1, x2, y2 = box
    draw.rounded_rectangle(box, radius=22, fill=fill, outline=outline, width=3)
    draw.text((x1 + 28, y1 + 22), title, font=font(title_size, bold=True), fill=INK)
    if lines:
        draw.line((x1 + 28, y1 + 73, x2 - 28, y1 + 73), fill=outline, width=2)
        text_lines(draw, lines, x1 + 28, y1 + 94, size=body_size,
                   spacing=line_spacing, color=MUTED)


def arrow(draw, points, color=NAVY, width=5, head=17, dash=False):
    if dash:
        for a, b in zip(points, points[1:]):
            distance = math.dist(a, b)
            if not distance:
                continue
            ux, uy = (b[0] - a[0]) / distance, (b[1] - a[1]) / distance
            pos = 0
            while pos < distance:
                end = min(pos + 17, distance)
                draw.line((a[0] + ux * pos, a[1] + uy * pos,
                           a[0] + ux * end, a[1] + uy * end), fill=color, width=width)
                pos += 28
    else:
        draw.line(points, fill=color, width=width, joint="curve")
    a, b = points[-2], points[-1]
    theta = math.atan2(b[1] - a[1], b[0] - a[0])
    wing = head * 0.55
    left = (b[0] - head * math.cos(theta) + wing * math.sin(theta),
            b[1] - head * math.sin(theta) - wing * math.cos(theta))
    right = (b[0] - head * math.cos(theta) - wing * math.sin(theta),
             b[1] - head * math.sin(theta) + wing * math.cos(theta))
    draw.polygon((b, left, right), fill=color)


def note(draw, box, title, lines, fill=AMBER_BG):
    x1, y1, x2, y2 = box
    draw.rounded_rectangle(box, radius=20, fill=fill, outline="#ead7aa", width=3)
    draw.text((x1 + 27, y1 + 15), title, font=font(27, bold=True), fill=INK)
    draw.line((x1 + 27, y1 + 62, x2 - 27, y1 + 62), fill="#ead7aa", width=2)
    text_lines(draw, lines, x1 + 27, y1 + 77, size=22, spacing=34, color=MUTED)


def render_hld():
    image, d = new_canvas(2600, 1890, "BookMyShow high-level design",
                          "Actual components and request paths in the current application")

    # The client and SQS queue are outside the application process.
    card(d, (1020, 215, 1580, 340), "HTTP client", ["Browser / Postman / curl"],
         TEAL_BG, "#a7dcd8", 32, 25)
    d.rounded_rectangle((85, 400, 2515, 1650), radius=30,
                        fill="#fcfdff", outline="#b7cbe1", width=4)
    d.text((130, 420), "ONE SPRING BOOT PROCESS", font=font(32, bold=True), fill=NAVY)
    d.text((130, 460), "REST API", font=font(24, bold=True), fill=MUTED)
    d.text((130, 785), "APPLICATION SERVICES", font=font(24, bold=True), fill=MUTED)
    d.text((130, 1180), "STATE, PRICING, AND ADAPTERS", font=font(24, bold=True), fill=MUTED)

    # External entry point and controller-to-service calls.
    arrow(d, [(1300, 340), (1300, 400)], TEAL, 6)
    d.text((1330, 365), "HTTP JSON", font=font(22, bold=True), fill=TEAL)
    arrow(d, [(445, 715), (615, 830)], NAVY, 4)
    arrow(d, [(1020, 715), (930, 830)], NAVY, 4)
    arrow(d, [(1590, 715), (1130, 830)], NAVY, 4)
    arrow(d, [(1630, 715), (1670, 830)], PURPLE, 4)
    arrow(d, [(2180, 715), (2130, 830)], PURPLE, 4)
    arrow(d, [(1570, 960), (1190, 960)], PURPLE, 4)

    # Service-to-state and service-to-adapter calls.
    arrow(d, [(770, 1085), (770, 1240)], TEAL, 4)
    arrow(d, [(1090, 1085), (1160, 1150), (1160, 1480)], TEAL, 4)
    arrow(d, [(1750, 1085), (1650, 1240)], PURPLE, 4)
    arrow(d, [(2140, 1085), (2150, 1240)], PURPLE, 4)
    arrow(d, [(2300, 960), (2440, 960), (2440, 1540), (2350, 1540)], PURPLE, 4)
    arrow(d, [(1425, 1640), (1425, 1730)], AMBER, 5, dash=True)

    # REST API: each controller has a concrete responsibility.
    card(d, (180, 500, 710, 715), "MovieController", [
        "Search title / language / city", "Future showtimes"], BLUE_BG, "#bfd5f0", 31, 24, 39)
    card(d, (755, 500, 1285, 715), "TheaterController", [
        "Search theaters by city", "List theater showtimes"], BLUE_BG, "#bfd5f0", 31, 24, 39)
    card(d, (1330, 500, 1860, 715), "BookingController", [
        "Create / read / cancel booking", "Quote ticket price"], BLUE_BG, "#bfd5f0", 31, 24, 39)
    card(d, (1905, 500, 2435, 715), "PaymentController", [
        "Pay for booking", "Read payment"], BLUE_BG, "#bfd5f0", 31, 24, 39)
    d.text((1590, 456), "BookingExceptionHandler: HTTP error mapping",
           font=font(21), fill=MUTED)

    card(d, (445, 830, 1190, 1085), "BookingSystem", [
        "Searches catalog; reserves and cancels seats", "Coordinates idempotency and booking events"],
         TEAL_BG, "#a7dcd8", 34, 25, 44)
    card(d, (1570, 830, 2300, 1085), "PaymentService", [
        "Quotes and validates payment amount", "Stores one payment per booking"],
         PURPLE_BG, "#d0bfef", 34, 25, 44)

    card(d, (265, 1240, 1130, 1580), "In-memory booking state", [
        "Theater / Movie / Showtime catalog", "Showtime seat set + ReentrantLock",
        "Reservations + IdempotencySlot results"], TEAL_BG, "#a7dcd8", 32, 25, 51)
    card(d, (1240, 1240, 1810, 1415), "Payment state", [
        "Payment map by booking ID"], PURPLE_BG, "#d0bfef", 30, 24)
    card(d, (1870, 1240, 2435, 1415), "TicketPricingService", [
        "Third-ticket + afternoon discounts"], PURPLE_BG, "#d0bfef", 29, 23)
    card(d, (1160, 1480, 1690, 1640), "BookingEventPublisher", [
        "SQS transport when configured"], AMBER_BG, "#ecd7ae", 29, 23)
    card(d, (1870, 1480, 2350, 1640), "PaymentGateway", [
        "Simulated CARD / UPI"], PURPLE_BG, "#d0bfef", 29, 23)
    card(d, (1160, 1730, 1690, 1855), "Amazon SQS (optional)", [],
         AMBER_BG, "#ecd7ae", 30)
    d.text((100, 1740), "No database. No real charge.", font=font(28, bold=True), fill=AMBER)
    d.text((100, 1782), "State resets on restart; seat lock", font=font(23), fill=MUTED)
    d.text((100, 1815), "protects one process only.", font=font(23), fill=MUTED)
    image.save(OUT / "hld.png", optimize=True)


def render_sequence():
    image, d = new_canvas(2400, 1710, "Booking sequence",
                          "POST /v1/bookings with Idempotency-Key")
    names = ["Client", "BookingController", "BookingSystem", "IdempotencySlot",
             "Showtime", "EventPublisher"]
    xs = [170, 585, 1000, 1415, 1830, 2245]
    widths = [265, 330, 320, 325, 275, 285]
    for x, name, w in zip(xs, names, widths):
        d.rounded_rectangle((x - w // 2, 230, x + w // 2, 315), radius=18,
                            fill=BLUE_BG if name != "Showtime" else TEAL_BG,
                            outline="#bcd2e9", width=3)
        bounds = d.textbbox((0, 0), name, font=font(24, bold=True))
        d.text((x - (bounds[2] - bounds[0]) / 2, 255), name,
               font=font(24, bold=True), fill=INK)
        # Dashed lifeline.
        for y in range(325, 1380, 28):
            d.line((x, y, x, min(y + 16, 1380)), fill="#b7c9df", width=3)

    steps = [
        (380, 0, 1, "1  POST booking + key"),
        (475, 1, 2, "2  book(showtime, seats, key)"),
        (570, 2, 3, "3  register or find key"),
        (800, 2, 4, "4  reserve requested seats"),
        (1010, 4, 2, "5  booked / conflict / busy"),
        (1100, 2, 5, "6  save + publish event"),
        (1190, 2, 3, "7  complete original result"),
        (1280, 2, 1, "8  reservation / error"),
        (1370, 1, 0, "9  201 + bookingId"),
    ]
    for y, start, end, label in steps:
        left, right = xs[start], xs[end]
        arrow(d, [(left, y), (right, y)], color=TEAL if start > end else NAVY,
              width=4, head=15, dash=start > end)
        mid = (left + right) / 2
        bounds = d.textbbox((0, 0), label, font=font(22, bold=True))
        d.text((mid - (bounds[2] - bounds[0]) / 2, y - 40), label,
               font=font(22, bold=True), fill=INK)

    note(d, (1040, 625, 1665, 740), "Retry branch", [
        "Same input reuses result; changed input returns 409."
    ], fill=PURPLE_BG)
    note(d, (1655, 850, 2300, 965), "Seat critical section", [
        "Validate; tryLock(50 ms); check and add seats."
    ], fill=TEAL_BG)
    note(d, (90, 1435, 2300, 1600), "Failure behavior", [
        "Seat conflict: 409. Lock timeout: 503 + Retry-After. Failed attempts release the idempotency key.",
        "SQS errors are logged after the booking is stored; they do not undo the reservation."
    ])
    image.save(OUT / "sequence.png", optimize=True)


def render_uml():
    image, d = new_canvas(2500, 1810, "UML class diagram",
                          "Current domain, services, and Strategy interfaces")
    d.text((94, 208), "DOMAIN MODEL", font=font(29, bold=True), fill=TEAL)
    d.text((915, 208), "APPLICATION SERVICES", font=font(29, bold=True), fill=NAVY)
    d.text((1760, 208), "EXTENSION POINTS", font=font(29, bold=True), fill=PURPLE)

    # Relationship lines stay behind the class cards.
    arrow(d, [(900, 385), (715, 385)], TEAL, 4)  # booking system uses theater
    arrow(d, [(900, 420), (715, 735)], TEAL, 4)  # booking system uses showtime
    arrow(d, [(900, 1350), (715, 1150)], TEAL, 4)  # payment reads reservation
    arrow(d, [(535, 500), (535, 560)], TEAL, 4)  # theater contains showtimes
    arrow(d, [(275, 560), (275, 475)], TEAL, 4)  # showtime presents movie
    arrow(d, [(335, 905), (335, 990)], TEAL, 4)  # showtime stores reservations
    arrow(d, [(1270, 555), (1270, 615)], NAVY, 4)  # booking uses slot
    arrow(d, [(1270, 1250), (1270, 1190)], NAVY, 4)  # payment uses pricing
    arrow(d, [(1615, 405), (1740, 1370)], PURPLE, 4)  # events
    arrow(d, [(1615, 1075), (1740, 550)], PURPLE, 4)  # discounts
    arrow(d, [(1615, 1390), (1740, 975)], PURPLE, 4)  # gateways

    card(d, (80, 265, 375, 475), "Movie", ["id: String", "title: String", "language: String"],
         TEAL_BG, "#b0dfda", 31, 24, 34)
    card(d, (405, 265, 715, 500), "Theater", ["id: String", "city: String",
        "showtimes: List"], TEAL_BG, "#b0dfda", 31, 24, 34)
    card(d, (80, 560, 715, 905), "Showtime", ["movie: Movie; theater: Theater",
        "bookedSeatIds: Set<String>", "seatLock: ReentrantLock", "+book(reservation); +cancel(reservation)"],
         TEAL_BG, "#b0dfda", 31, 24, 48)
    card(d, (80, 990, 715, 1245), "Reservation", ["bookingId: String",
        "showtime: Showtime", "seatIds: List<String>"], TEAL_BG, "#b0dfda", 31, 24, 43)
    note(d, (80, 1325, 715, 1640), "Relationships", [
        "Theater contains showtimes.", "Showtime presents a movie.",
        "Showtime stores reservations.", "Seat updates occur under its lock."
    ], fill=TEAL_BG)

    card(d, (900, 265, 1615, 555), "BookingSystem", [
        "+searchMovies(title, language, city)", "+book(showtimeId, seatIds, key)",
        "+cancelReservation(bookingId)", "reservations + idempotency maps"],
         BLUE_BG, "#bfd5f0", 32, 24, 44)
    card(d, (900, 615, 1615, 860), "IdempotencySlot", [
        "+matches(input); +awaitResult()", "+completeSuccess(reservation)"],
         BLUE_BG, "#bfd5f0", 32, 24, 45)
    card(d, (900, 930, 1615, 1190), "TicketPricingService", [
        "+quote(reservation): PriceQuote", "uses eligible city + theater rules"],
         BLUE_BG, "#bfd5f0", 32, 24, 45)
    card(d, (900, 1250, 1615, 1570), "PaymentService", [
        "+quote(bookingId); +pay(...) ", "+cancelUnpaidBooking(bookingId)",
        "paymentsByBookingId: Map"], BLUE_BG, "#bfd5f0", 32, 24, 45)

    card(d, (1740, 265, 2420, 625), "DiscountPolicy <<interface>>", [
        "+discount(ticketNumber, hour, price)", "ThirdTicketDiscountPolicy",
        "AfternoonDiscountPolicy"], PURPLE_BG, "#d0bfef", 31, 24, 51)
    card(d, (1740, 700, 2420, 1100), "PaymentGateway <<interface>>", [
        "+method(): PaymentMethod", "+charge(paymentId, amount, INR)",
        "SimulatedCardGateway", "SimulatedUpiGateway"], PURPLE_BG, "#d0bfef", 31, 24, 51)
    card(d, (1740, 1180, 2420, 1510), "BookingEventPublisher", [
        "+publish(reservation)", "SqsBookingEventPublisher", "no-op when queue URL is empty"],
         PURPLE_BG, "#d0bfef", 31, 24, 47)
    d.text((91, 1700), "Solid arrows show use/dependency. Strategy implementations are listed inside their interfaces.",
           font=font(24), fill=MUTED)
    image.save(OUT / "uml.png", optimize=True)


if __name__ == "__main__":
    render_hld()
    render_sequence()
    render_uml()
    for path in (OUT / "hld.png", OUT / "sequence.png", OUT / "uml.png"):
        print(path.name, path.stat().st_size)
