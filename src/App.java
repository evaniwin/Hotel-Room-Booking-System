import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

enum ROOM_TYPE {
    SINGLE, DOUBLE, DELUXE
}

class Room {
    private final int roomNumber;
    private ROOM_TYPE type;
    private int pricePerNight;

    public Room(int roomNumber, ROOM_TYPE type, int pricePerNight) {
        if (pricePerNight < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public ROOM_TYPE getType() {
        return type;
    }

    public int getPricePerNight() {
        return pricePerNight;
    }

    public void setType(ROOM_TYPE type) {
        this.type = Objects.requireNonNull(type);
    }

    public void setPricePerNight(int pricePerNight) {
        if (pricePerNight < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.pricePerNight = pricePerNight;
    }

    public String DetailstoString() {
        return "Room{" + roomNumber + ", " + type + ", price=" + pricePerNight + "}";
    }
}

class Guest {
    private final int id;
    private String name;
    private String phone;
    private String email;
    private final ArrayList<Integer> bookingIds;

    public Guest(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.bookingIds = new ArrayList<Integer>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public List<Integer> getBookingIds() {
        return bookingIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    void addBookingId(int bookingId) {
        bookingIds.add(bookingId);
    }

    void removeBookingId(int bookingId) {
        bookingIds.remove((Integer) bookingId);
    }

    public String DetailstoString() {
        return "Guest{" + id + ", name='" + name + "'" + ", phone='" + phone + "'" + ", email='" + email + "'" + "}";
    }
}

class Booking {
    private final int id;
    private final int guestId;
    private final int roomNumber;
    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public Booking(int id, int guestId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("checkOut must be after checkIn");
        this.id = id;
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public int getId() {
        return id;
    }

    public int getGuestId() {
        return guestId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean overlaps(LocalDate start, LocalDate endExclusive) {
        // Overlap if start < this.checkOut && end > this.checkIn
        return start.isBefore(this.checkOut) && endExclusive.isAfter(this.checkIn);
    }

    public String DetailstoString() {
        return "Booking{" + id + ", guest=" + guestId + ", room=" + roomNumber + ", " + checkIn + "->" + checkOut + " ("
                + nights() + " nights)}";
    }
}

class HotelManager {
    private final Map<Integer, Room> rooms = new HashMap<>();
    private final Map<Integer, Guest> guests = new HashMap<>();
    private final Map<Integer, Booking> bookings = new HashMap<>();

    private final AtomicInteger nextGuestId = new AtomicInteger(1);
    private final AtomicInteger nextBookingId = new AtomicInteger(1);

    // ---------------- Room Management ----------------
    public void addRoom(Room room) {
        if (rooms.containsKey(room.getRoomNumber()))
            throw new IllegalArgumentException("Room already exists: " + room.getRoomNumber());
        rooms.put(room.getRoomNumber(), room);
    }

    // Use CancelallroomBookings before calling
    public Room removeRoom(int roomNumber) {
        // Ensure no future bookings exist
        LocalDate today = LocalDate.now();
        for (Booking b : bookings.values()) {
            if (b.getRoomNumber() == roomNumber && (b.getCheckOut().isAfter(today)))
                throw new IllegalStateException("Cannot remove room with active/future bookings: " + roomNumber);
        }
        clearRoomBookingData(roomNumber);
        // returns the removed room if found else return null
        return rooms.remove(roomNumber);
    }

    private void clearRoomBookingData(int roomNumber) {
        for (Booking b : bookings.values()) {
            if (b.getRoomNumber() == roomNumber)
                cancelBooking(b.getId());
        }
    }

    public void CancelAllroomBookings(int roomNumber) {
        LocalDate today = LocalDate.now();
        for (Booking b : bookings.values()) {
            if (b.getRoomNumber() == roomNumber && (b.getCheckOut().isAfter(today)))
                cancelBooking(b.getId());
        }
    }

    public void updateRoomPrice(int roomNumber, int newPrice) {
        Room r = requireRoom(roomNumber);
        r.setPricePerNight(newPrice);
    }

    public void updateRoomType(int roomNumber, ROOM_TYPE newType) {
        Room r = requireRoom(roomNumber);
        r.setType(newType);
    }

    public List<Room> listAvailableRoomsByType(ROOM_TYPE type, LocalDate start, LocalDate endExclusive) {
        List<Room> result = new ArrayList<>();
        for (Room r : rooms.values()) {
            if (r.getType() == type && isRoomAvailable(r.getRoomNumber(), start, endExclusive)) {
                result.add(r);
            }
        }
        return result;
    }

    public boolean isRoomAvailable(int roomNumber, LocalDate start, LocalDate endExclusive) {
        requireRoom(roomNumber);
        for (Booking b : bookings.values()) {
            if (b.getRoomNumber() == roomNumber && b.overlaps(start, endExclusive)) {
                return false;
            }
        }
        return true;
    }

    // ---------------- Guest Management ----------------
    public Guest registerGuest(String name, String phone, String email) {
        int id = nextGuestId.getAndIncrement();
        Guest g = new Guest(id, name, phone, email);
        guests.put(id, g);
        return g;
    }

    public Guest getGuest(int guestId) {
        return requireGuest(guestId);
    }

    // TODO Remove/unregister guest

    // ---------------- Booking Operations ----------------
    public Booking bookRoom(int guestId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Guest g = requireGuest(guestId);
        Room r = requireRoom(roomNumber);
        if (!isRoomAvailable(roomNumber, checkIn, checkOut)) {
            throw new IllegalStateException("Room " + roomNumber + " is not available for the selected dates");
        }
        int bookingId = nextBookingId.getAndIncrement();
        Booking b = new Booking(bookingId, g.getId(), r.getRoomNumber(), checkIn, checkOut);
        bookings.put(bookingId, b);
        g.addBookingId(bookingId);
        return b;
    }

    public void cancelBooking(int bookingId) {
        Booking b = bookings.remove(bookingId);
        if (b == null)
            throw new NoSuchElementException("Booking not found: " + bookingId);
        Guest g = guests.get(b.getGuestId());
        if (g != null)
            g.removeBookingId(bookingId);
    }

    public long calculateBill(int bookingId) {
        Booking b = requireBooking(bookingId);
        Room r = requireRoom(b.getRoomNumber());
        return b.nights() * (long) r.getPricePerNight();
    }

    // ---------------- Helpers ----------------
    Room requireRoom(int roomNumber) {
        Room r = rooms.get(roomNumber);
        if (r == null)
            throw new NoSuchElementException("Room not found: " + roomNumber);
        return r;
    }

    private Guest requireGuest(int guestId) {
        Guest g = guests.get(guestId);
        if (g == null)
            throw new NoSuchElementException("Guest not found: " + guestId);
        return g;
    }

    private Booking requireBooking(int bookingId) {
        Booking b = bookings.get(bookingId);
        if (b == null)
            throw new NoSuchElementException("Booking not found: " + bookingId);
        return b;
    }

    public Collection<Room> allRooms() {
        return rooms.values();
    }

    public Collection<Guest> allGuests() {
        return guests.values();
    }

    public Collection<Booking> allBookings() {
        return bookings.values();
    }
}

public class App {
    public static void main(String[] args) {
        HotelManager manager = new HotelManager();
        test(manager);

    }

    static void test(HotelManager manager) {
        // Seed rooms
        manager.addRoom(new Room(101, ROOM_TYPE.SINGLE, 2000));
        manager.addRoom(new Room(102, ROOM_TYPE.SINGLE, 2200));
        manager.addRoom(new Room(201, ROOM_TYPE.DOUBLE, 3500));
        manager.addRoom(new Room(301, ROOM_TYPE.DELUXE, 6000));

        // Register guests
        Guest alice = manager.registerGuest("Alice", "9990001111", "alice@example.com");
        Guest bob = manager.registerGuest("Bob", "9990002222", "bob@example.com");

        LocalDate in = LocalDate.of(2025, 8, 20);
        LocalDate out = LocalDate.of(2025, 8, 23); // 3 nights

        // Check availability & book
        System.out.println("Available SINGLE rooms");
        for (Room r : manager.listAvailableRoomsByType(ROOM_TYPE.SINGLE, in, out)) {
            System.out.println("  " + r.getRoomNumber());
        }

        Booking b1 = manager.bookRoom(alice.getId(), 101, in, out);
        System.out.println("Booked: " + b1);
        System.out.println("Bill for booking " + b1 + ": " + manager.calculateBill(b1.getId()));

        // Attempt overlapping booking for the same room (should fail)
        try {
            manager.bookRoom(bob.getId(), 101, LocalDate.of(2025, 8, 21), LocalDate.of(2025, 8, 22));
        } catch (Exception e) {
            System.out.println("Expected failure: " + e.getMessage());
        }

        // Book a different available room for Bob
        Booking b2 = manager.bookRoom(bob.getId(), 102, LocalDate.of(2025, 8, 21), LocalDate.of(2025, 8, 22));
        System.out.println("Booked: " + b2);
        System.out.println("Bill for booking " + b2 + ": " + manager.calculateBill(b2.getId()));

        // Cancel Bob's booking
        manager.cancelBooking(b2.getId());
        System.out.println("Cancelled booking " + b2);

        // Update room details
        manager.updateRoomPrice(201, 3800);
        manager.updateRoomType(201, ROOM_TYPE.DELUXE);
        System.out.println("Updated room : " + manager.requireRoom(201).getRoomNumber());

        // Final lists
        System.out.println("All bookings:");
        for (Booking b : manager.allBookings())
            System.out.println("  " + b);

    }
}
