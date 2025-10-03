import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;

enum ROOM_TYPE {
    SINGLE, DOUBLE, DELUXE;

    public static ROOM_TYPE enumfromint(int val) {
        switch (val) {
            case 1:
                return ROOM_TYPE.SINGLE;
            case 2:
                return ROOM_TYPE.DOUBLE;
            case 3:
                return ROOM_TYPE.DELUXE;
            default:
                throw new IllegalArgumentException("Given Value must be between 1 and 3");
        }
    }
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

    // TODO REMOVE METHOD
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

    public boolean PrintRoomAvailable(LocalDate start, LocalDate endExclusive) {
        System.out.println("Available Rooms For Selected Date");
        System.out.println();
        for (Room room : rooms.values()) {
            for (Booking b : bookings.values()) {
                if (b.getRoomNumber() == room.getRoomNumber() && !b.overlaps(start, endExclusive)) {
                    System.out.println("");
                    System.out.println("    " + b);
                    System.out.println("        | Room Number: " + room.getRoomNumber());
                    System.out.println("        | Room Type: " + room.getType());
                    System.out.println("        | Price Per Night: " + room.getPricePerNight());
                    System.out.println("");
                }
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

    public Guest removeGuest(int guestid) {
        // cancel all bookings
        for (Booking b : bookings.values()) {
            if (b.getGuestId() == guestid)
                cancelBooking(b.getId());
        }
        return guests.remove(guestid);
    }

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

    public Guest requireGuest(int guestId) {
        Guest g = guests.get(guestId);
        if (g == null)
            throw new NoSuchElementException("Guest not found: " + guestId);
        return g;
    }

    public Booking requireBooking(int bookingId) {
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

class Cardpanel {
    JPanel main;
    JPanel sidebar;
    JPanel mainpanel;
    JPanel psidenav;
    JPanel psideinfo;
    JScrollPane scrollPanemainpanel;
    JPanel innerTiles;

    Cardpanel(int pan, ActionListener navlist) {
        GridBagConstraints con = new GridBagConstraints();
        con.fill = GridBagConstraints.BOTH;
        main = new JPanel(new GridBagLayout());
        main.setBackground(new Color(100, 100, 100));
        main.setBorder(new EmptyBorder(5, 5, 5, 5));
        sidebar = new JPanel(new GridBagLayout());
        sidebar.setOpaque(false);
        mainpanel = new JPanel(new GridBagLayout());
        con.gridx = 0;
        con.gridy = 0;
        con.gridheight = 1;
        con.gridwidth = 1;
        con.weightx = 1.0;
        con.weighty = 1.0;
        createsidepanel(pan, navlist);
        main.add(sidebar, con);
        con.gridx = 1;
        con.gridy = 0;
        con.gridheight = 1;
        con.gridwidth = 3;
        con.weightx = 3.0;
        con.weighty = 1.0;
        configuremainpanel();
        mainpanel.setOpaque(true);
        main.add(mainpanel, con);

    }

    void createsidepanel(int pan, ActionListener navlist) {

        psidenav = createNavPanel(pan, navlist);
        psideinfo = createControlsPanel();

        GridBagConstraints constr = new GridBagConstraints();
        constr.fill = GridBagConstraints.BOTH;
        constr.weightx = 1.0;

        constr.gridx = 0;
        constr.gridy = 0;
        constr.weighty = 1.0;
        constr.insets = new Insets(0, 0, 5, 0);
        sidebar.add(psidenav, constr);

        constr.gridy = 1;
        constr.weighty = 4.0;
        constr.insets = new Insets(0, 0, 0, 0);
        sidebar.add(psideinfo, constr);
    }

    private JPanel createNavPanel(int pan, ActionListener navlist) {

        JPanel navPanel = new JPanel(new GridBagLayout());
        navPanel.setBackground(Color.LIGHT_GRAY);

        // Set the border
        Border lineBorder = new MatteBorder(3, 3, 3, 1, Color.GRAY);
        Border paddingBorder = new EmptyBorder(0, 10, 10, 0);
        navPanel.setBorder(new CompoundBorder(lineBorder, paddingBorder));

        GridBagConstraints constr = new GridBagConstraints();
        constr.fill = GridBagConstraints.BOTH;
        constr.weightx = 1.0;
        constr.weighty = 1.0;
        constr.gridx = 0;

        // Create and add the title label
        JLabel titleLabel = new JLabel("Management Menu");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Font boldFont = new Font(titleLabel.getFont().getName(), Font.BOLD, titleLabel.getFont().getSize());
        titleLabel.setFont(boldFont);
        constr.gridy = 0;
        navPanel.add(titleLabel, constr);

        // Create buttons
        Color maincol = new Color(150, 150, 150);
        Color selectcol = Color.GRAY;
        Color hoverColor = new Color(130, 180, 190);
        String[] buttonLabels = { "Home", "Room Management", "Guest Management", "Booking Management" };
        String[] images = { "icons/go-home.png", "icons/drive-multidisk.png", "icons/im-user.png",
                "icons/address-book-new.png" };
        for (int i = 0; i < buttonLabels.length; i++) {
            JButton button = new JButton();
            JPanel panel = new JPanel(new BorderLayout());
            JLabel iconLabel = new JLabel(new ImageIcon(images[i]));
            JLabel textLabel = new JLabel(buttonLabels[i], SwingConstants.CENTER);
            panel.setOpaque(false);
            button.setLayout(new BorderLayout());
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(textLabel, BorderLayout.CENTER);
            button.setActionCommand(buttonLabels[i]);
            button.add(panel);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setFocusPainted(false);
            Color baseColor;
            if (pan == i) {
                baseColor = selectcol;
            } else {
                baseColor = maincol;
            }
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (baseColor != selectcol) {
                        button.setBackground(hoverColor);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(baseColor);
                }
            });
            button.setBackground(baseColor);
            button.addActionListener(navlist);
            constr.gridy = i + 1;
            navPanel.add(button, constr);
        }

        return navPanel;
    }

    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setBackground(Color.LIGHT_GRAY);

        // Set the border
        Border lineBorder = new MatteBorder(3, 3, 3, 3, Color.GRAY);
        Border paddingBorder = new EmptyBorder(10, 10, 10, 10);
        controlsPanel.setBorder(new CompoundBorder(lineBorder, paddingBorder));

        return controlsPanel;
    }

    private void configuremainpanel() {
        GridBagConstraints con = new GridBagConstraints();
        Color maincol = new Color(230, 230, 230);
        con.fill = GridBagConstraints.BOTH;
        JPanel additionbuttons = new JPanel(new BorderLayout());
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 0;
        con.fill = GridBagConstraints.HORIZONTAL;
        additionbuttons.setPreferredSize(new Dimension(0, 50));
        additionbuttons.setBackground(maincol);
        Border lineBorder = new MatteBorder(3, 3, 0, 3, Color.GRAY);
        Border paddingBorder = new EmptyBorder(10, 10, 10, 10);
        additionbuttons.setBorder(new CompoundBorder(lineBorder, paddingBorder));

        JButton button = new JButton("Create New",new ImageIcon("icons/list-add.png"));
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFocusPainted(false);
        additionbuttons.add(button, BorderLayout.EAST);
        Color basecol = new Color(160, 160, 160);
        Color hovercol = new Color(130, 180, 190);
        button.setBackground(basecol);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hovercol);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(basecol);
            }
        });

        mainpanel.add(additionbuttons, con);

        innerTiles = new JPanel(new GridBagLayout());
        scrollPanemainpanel = new JScrollPane(innerTiles, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        con.fill = GridBagConstraints.BOTH;
        innerTiles.setBackground(maincol);
        scrollPanemainpanel
                .setBorder(new CompoundBorder(new MatteBorder(0, 3, 3, 3, Color.GRAY), new EmptyBorder(2, 2, 2, 2)));
        mainpanel.add(scrollPanemainpanel, con);
    }

    private void configureinfopanel() {

    }
}

class Navevent implements ActionListener {
    CardLayout cardlayout;
    JFrame frame;

    Navevent(CardLayout cardlayout, JFrame frame) {
        this.cardlayout = cardlayout;
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        String[] buttonLabels = { "Home", "Room Management", "Guest Management", "Booking Management" };
        String[] cardLabels = { "Home", "room", "guest", "booking" };
        for (int i = 0; i < buttonLabels.length; i++) {
            if (cmd == buttonLabels[i]) {
                cardlayout.show(frame.getContentPane(), cardLabels[i]);
            }
        }
    }
}

class AppUI {
    JFrame frame;

    Cardpanel home;
    Cardpanel roommgmt;
    Cardpanel guestmgmt;
    Cardpanel bookingmgmt;

    CardLayout cardlayout;

    AppUI() {
        frame = new JFrame();
        frame.setSize(new Dimension(1200, 800));
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setTitle("Hotel Management System");
        cardlayout = new CardLayout(0, 0);
        frame.setLayout(cardlayout);
        ActionListener navlist = new Navevent(cardlayout, frame);
        // Setub card menu and panels
        home = new Cardpanel(0, navlist);
        frame.add(home.main, "Home");
        roommgmt = new Cardpanel(1, navlist);
        frame.add(roommgmt.main, "room");
        guestmgmt = new Cardpanel(2, navlist);
        frame.add(guestmgmt.main, "guest");
        bookingmgmt = new Cardpanel(3, navlist);
        frame.add(bookingmgmt.main, "booking");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}

public class App {
    public static void main(String[] args) {
        HotelManager manager = new HotelManager();
        AppUI ui = new AppUI();
        Scanner sc = new Scanner(System.in);
        // while (true) {
        // clear();
        // System.out.println("This is the Hotel administration Menu");
        // System.out.println(" 1) Room Management");
        // System.out.println(" 2) Guest Management");
        // System.out.println(" 3) Booking Management");
        // System.out.println(" 4) Run Tests");
        // System.out.println(" 5) Exit Program");
        // System.out.println("Enter (1/2/3/4/5)?");
        // System.out.print('>');
        // int op = sc.nextInt();
        // clear();
        // switch (op) {
        // // room
        // case 1:
        // manageroom(manager, sc);
        // break;
        // // guest
        // case 2:
        // manageguest(manager, sc);
        // break;
        // // booking
        // case 3:
        // managebooking(manager, sc);
        // break;
        // // Test
        // case 4:
        // test();
        // System.out.println();
        // System.out.println("Tests Completed");
        // System.out.println("Press Enter to continue");
        // System.out.println();
        // sc.nextLine();
        // sc.nextLine();
        // break;
        // // Exit
        // case 5:
        // sc.close();
        // return;
        // default:
        // System.out.println("Invalid Option Selected");
        // System.out.println("Press Enter to continue");
        // sc.nextLine();
        // sc.nextLine();
        // break;
        // }
        // }
    }

    static void manageroom(HotelManager manager, Scanner sc) {
        int roomno, choice;
        Room room;
        ROOM_TYPE type;
        while (true) {
            clear();
            System.out.println("This is the Hotel Room management Menu");
            System.out.println("    1) Create Room");
            System.out.println("    2) View Created Rooms");
            System.out.println("    3) Edit Room Details");
            System.out.println("    4) Delete Room");
            System.out.println("    5) Exit Menu");
            System.out.println("Enter (1/2/3/4/5)?");
            System.out.print('>');
            int op = sc.nextInt();
            clear();
            switch (op) {
                // create room
                case 1:
                    try {
                        System.out.println("Enter Room Price Per night");
                        System.out.print('>');
                        int pricepernight = sc.nextInt();
                        clear();
                        System.out.println("Enter Room Type");
                        System.out.println("Available Options");
                        System.out.println("    1) Single room");
                        System.out.println("    2) Double room");
                        System.out.println("    3) Deluxe room");
                        System.out.println("Enter (1/2/3)?");
                        System.out.print('>');
                        type = ROOM_TYPE.enumfromint(sc.nextInt());
                        clear();
                        System.out.println("Enter Room Number To assign to room");
                        System.out.print('>');
                        int roomnum = sc.nextInt();
                        manager.addRoom(new Room(roomnum, type, pricepernight));
                    } catch (IllegalArgumentException e) {
                        System.out.println(e);
                        return;
                    }

                    break;
                // list all created rooms
                case 2:
                    System.out.println("Enter Room Type to display");
                    System.out.println("Available Options");
                    System.out.println("    1) Single room");
                    System.out.println("    2) Double room");
                    System.out.println("    3) Deluxe room");
                    System.out.println("    4) All rooms");
                    System.out.println("Enter (1/2/3/4)?");
                    System.out.print('>');
                    int selection = sc.nextInt();
                    clear();
                    try {
                        type = ROOM_TYPE.enumfromint(selection);
                        System.out.println("Existing Rooms of type '" + type + "' :");
                        for (Room b : manager.allRooms()) {
                            if (type != b.getType())
                                continue;
                            System.out.println("");
                            System.out.println("    " + b);
                            System.out.println("        | Room Number: " + b.getRoomNumber());
                            System.out.println("        | Room Type: " + b.getType());
                            System.out.println("        | Price Per Night: " + b.getPricePerNight());
                            System.out.println("");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Existing Rooms of all types");
                        for (Room b : manager.allRooms()) {
                            System.out.println("");
                            System.out.println("    " + b);
                            System.out.println("        | Room Number: " + b.getRoomNumber());
                            System.out.println("        | Room Type: " + b.getType());
                            System.out.println("        | Price Per Night: " + b.getPricePerNight());
                            System.out.println("");
                        }
                    }

                    System.out.println("Press Enter to continue");
                    sc.nextLine();
                    sc.nextLine();
                    break;
                // edit a given room
                case 3:
                    System.out.println("Enter The room number to Edit");
                    System.out.print('>');
                    roomno = sc.nextInt();

                    try {
                        clear();
                        room = manager.requireRoom(roomno);
                        System.out.println("Available Parameters");
                        System.out.println("    1) Change Price per Night");
                        System.out.println("    2) Change Room Type");
                        System.out.println("Enter The room number to Edit (1/2)?");
                        System.out.print('>');
                        choice = sc.nextInt();
                        clear();
                        if (choice == 1) {
                            System.out.println("Currently set Price Per Night is " + room.getPricePerNight());
                            System.out.println("Enter The New Price Per Night to Set");
                            System.out.print('>');
                            room.setPricePerNight(sc.nextInt());
                        } else if (choice == 2) {
                            System.out.println("Currently set Room Type is " + room.getType());
                            System.out.println("Enter The New Room Type to Set");
                            System.out.println("Enter Room Type");
                            System.out.println("Available Options");
                            System.out.println("    1) Single room");
                            System.out.println("    2) Double room");
                            System.out.println("    3) Deluxe room");
                            System.out.println("Enter (1/2/3)?");
                            System.out.print('>');
                            room.setType(ROOM_TYPE.enumfromint(sc.nextInt()));
                        } else {
                            System.out.println("Invalid Option Selected");
                            System.out.println("Press Enter to continue");
                            sc.nextLine();
                            sc.nextLine();
                        }

                        System.out.print('>');
                    } catch (NoSuchElementException e) {
                        clear();
                        System.out.println("Room " + roomno + " Not Found");
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                    } catch (NullPointerException e) {
                        System.out.println(e);
                    }
                    break;
                // delete a given room
                case 4:
                    System.out.println("Enter The room number to Delete");
                    System.out.print('>');
                    roomno = sc.nextInt();
                    try {
                        clear();
                        room = manager.requireRoom(roomno);

                    } catch (NoSuchElementException e) {
                        System.out.println("Room " + roomno + " Not Found");
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                        break;
                    }
                    System.out.println("Are you sure you want to delete Room " + room.getRoomNumber() + " (y/n) ");
                    System.out.println("    1) yes");
                    System.out.println("    2) No");
                    System.out.print('>');
                    choice = sc.nextInt();
                    if (choice == 1) {
                        clear();
                        System.out.println("Deleted Room NO: " + room.getRoomNumber());
                        manager.removeRoom(room.getRoomNumber());
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                    } else {
                        clear();
                        System.out.println("Cancelled room Deletion");
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                    }
                    break;
                // exit the sub menu
                case 5:
                    return;
                default:
                    System.out.println("Invalid Option Selected");
                    System.out.println("Press Enter to continue");
                    sc.nextLine();
                    sc.nextLine();
                    break;
            }
        }
    }

    static void manageguest(HotelManager manager, Scanner sc) {
        int guestid, choice;
        Guest guest;
        while (true) {
            clear();
            System.out.println("This is the Hotel Guest management Menu");
            System.out.println("    1) Register New Guest");
            System.out.println("    2) List Registered Guests");
            System.out.println("    3) Modify Guest Details");
            System.out.println("    4) Delete Guest Registeration");
            System.out.println("    5) Exit Menu");
            System.out.print('>');
            int op = sc.nextInt();
            clear();
            switch (op) {
                case 1:
                    System.out.println("Enter Details of the Guest to register");
                    System.out.println("Enter Guest Name");
                    System.out.print('>');
                    sc.nextLine();
                    String name = sc.nextLine();
                    System.out.println("Enter Guest Phonenumber");
                    System.out.print('>');
                    String phone = sc.next();
                    System.out.println("Enter Guest Email");
                    System.out.print('>');
                    String email = sc.next();
                    manager.registerGuest(name, phone, email);
                    break;
                // list guests
                case 2:
                    for (Guest g : manager.allGuests()) {
                        System.out.println("");
                        System.out.println("    " + g);
                        System.out.println("        | Guest Id: " + g.getId());
                        System.out.println("        | Guest Name: " + g.getName());
                        System.out.println("        | Guest Email: " + g.getEmail());
                        System.out.println("        | Guest Phone: " + g.getPhone());
                        System.out.println("        Bookings");
                        for (int id : g.getBookingIds()) {
                            System.out.println("            | Booking Id: " + id);
                        }
                        System.out.println("");
                    }
                    System.out.println("Press Enter to continue");
                    sc.nextLine();
                    sc.nextLine();
                    break;
                // edit guest
                case 3:
                    System.out.println("Enter Guest Id of whose parameters to edit");
                    System.out.print('>');
                    guestid = sc.nextInt();
                    try {
                        guest = manager.requireGuest(guestid);
                    } catch (NoSuchElementException e) {
                        System.out.println("The Entered guest id does not belong to any registered guest");
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                        break;
                    }
                    while (true) {
                        clear();
                        System.out.println("Current Guest Details");
                        System.out.println("    " + guest);
                        System.out.println("        | Guest Id: " + guest.getId());
                        System.out.println("        | Guest Name: " + guest.getName());
                        System.out.println("        | Guest Email: " + guest.getEmail());
                        System.out.println("        | Guest Phone: " + guest.getPhone());
                        System.out.println("        Bookings");
                        for (int id : guest.getBookingIds()) {
                            System.out.println("            | Booking Id: " + id);
                        }
                        System.out.println("");
                        System.out.println("Enter The Property of Guest '" + guestid + "' To modify");
                        System.out.println("    1) Modify Guest Name");
                        System.out.println("    2) Modify Guest Email");
                        System.out.println("    3) Modify Guest Phone number");
                        System.out.println("    4) Cancel Modification");
                        System.out.print('>');
                        choice = sc.nextInt();
                        switch (choice) {
                            case 1:
                                System.out.println("Enter New name For Guest");
                                System.out.print('>');
                                sc.nextLine();
                                guest.setName(sc.nextLine());
                                break;
                            case 2:
                                System.out.println("Enter New Email For Guest");
                                System.out.print('>');
                                sc.nextLine();
                                guest.setEmail(sc.nextLine());
                                break;
                            case 3:
                                System.out.println("Enter New Phone number For Guest");
                                System.out.print('>');
                                sc.nextLine();
                                guest.setPhone(sc.nextLine());
                                break;
                            case 4:
                                break;
                            default:
                                System.out.println("Invalid Option Selected");
                                System.out.println("Press Enter to continue");
                                sc.nextLine();
                                sc.nextLine();
                                continue;
                        }
                        break;
                    }
                    break;
                // delete guest
                case 4:
                    System.out.println("Enter The Guest Id of Guest to UnRegister");
                    System.out.print('>');
                    guestid = sc.nextInt();
                    try {
                        clear();
                        guest = manager.requireGuest(guestid);

                    } catch (NoSuchElementException e) {
                        System.out.println("Guest with Guest id:  " + guestid + " Not Found");
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                        break;
                    }
                    System.out.println("Are you sure you want to UnRegister guest " + guest.getId());
                    System.out.println("    1) yes");
                    System.out.println("    2) No");
                    System.out.print('>');
                    choice = sc.nextInt();
                    if (choice == 1) {
                        clear();
                        System.out.println("Unregistered Guest Id: " + guest.getId());
                        manager.removeGuest(guest.getId());
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                    } else {
                        clear();
                        System.out.println("Cancelled Guest Unregisteration");
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                        break;
                    }

                    break;
                // exit menu
                case 5:
                    return;
                default:
                    System.out.println("Invalid Option Selected");
                    System.out.println("Press Enter to continue");
                    sc.nextLine();
                    sc.nextLine();
                    break;
            }
        }
    }

    static void managebooking(HotelManager manager, Scanner sc) {
        LocalDate checkin;
        LocalDate checkout;
        int room, guestid, bookingid, op;
        while (true) {
            clear();
            System.out.println("This is the Hotel Booking management Menu");
            System.out.println("    1) Book Room");
            System.out.println("    2) View Bookings For Room");
            System.out.println("    3) Calculate Bill");
            System.out.println("    4) Cancel Booking");
            System.out.println("    5) Exit Menu");
            System.out.print('>');
            op = sc.nextInt();
            clear();
            switch (op) {
                // Book Room
                case 1:
                    checkin = getdatefromuser(sc, "Enter Checkin Date");
                    checkout = getdatefromuser(sc, "Enter Checkout Date");
                    while (true) {
                        clear();
                        System.out.println("Enter the room to book");
                        System.out.print('>');
                        room = sc.nextInt();
                        try {
                            manager.requireRoom(room);
                            if (!manager.isRoomAvailable(room, checkin, checkout)) {
                                System.out.println("The Room is already booked for the selected dates");
                                System.out.println("Press Enter to continue");
                                sc.nextLine();
                                sc.nextLine();
                                continue;

                            }
                            break;
                        } catch (NoSuchElementException e) {
                            System.out.println("The chosen Room Does not Exist");
                            System.out.println("Press Enter to continue");
                            sc.nextLine();
                            sc.nextLine();

                        }
                    }
                    manager.PrintRoomAvailable(checkin, checkout);
                    while (true) {
                        clear();
                        System.out.println("Enter the guest id of guest to book room  for");
                        System.out.print('>');
                        guestid = sc.nextInt();
                        try {
                            manager.getGuest(guestid);
                            break;
                        } catch (NoSuchElementException e) {
                            System.out.println("The chosen Guest Does not Exist");
                            System.out.println("Press Enter to continue");
                            sc.nextLine();
                            sc.nextLine();
                            continue;
                        }
                    }
                    manager.bookRoom(guestid, room, checkin, checkout);
                    break;
                // List bookings
                case 2:
                    System.out.println("Enter the room number of the room to view bookings for");
                    System.out.print(">");
                    room = sc.nextInt();
                    try {
                        manager.requireRoom(room);
                    } catch (Exception e) {
                        System.out.println("The room Entered is not a valid room");
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                        break;
                    }
                    System.out.println("Bookings For room no: " + room);
                    for (Booking b : manager.allBookings()) {
                        if (b.getRoomNumber() != room)
                            continue;
                        System.out.println("");
                        System.out.println("    " + b);
                        System.out.println("        | Booking id: " + b.getId());
                        System.out.println("        | Guest id: " + b.getGuestId());
                        System.out.println("        | CheckIn time: " + b.getCheckIn());
                        System.out.println("        | CheckOut time: " + b.getCheckOut());
                        System.out.println("");
                    }
                    System.out.println("Press Enter to continue");
                    sc.nextLine();
                    sc.nextLine();
                    break;
                case 3:
                    System.out.println("Enter the Booking id for which the bill must be calculated");
                    System.out.print('>');
                    bookingid = sc.nextInt();
                    try {
                        manager.requireBooking(bookingid);
                    } catch (Exception e) {
                        System.out.println("The entered booking is not a valid booking");
                        break;
                    }
                    System.out.println("The Bill for booking is :" + manager.calculateBill(bookingid));
                    System.out.println("Press Enter to continue");
                    sc.nextLine();
                    sc.nextLine();
                    break;
                case 4:
                    System.out.println("Enter Id of booking to cancel");
                    bookingid = sc.nextInt();
                    try {
                        manager.requireBooking(bookingid);
                        manager.cancelBooking(bookingid);
                    } catch (NoSuchElementException e) {
                        System.out.println("Invalid Booking Id selected");
                        System.out.println("Do you want to see all available bookings");
                        System.out.println("    1) yes");
                        System.out.println("    2) No");
                        System.out.print('>');
                        int choice = sc.nextInt();
                        if (choice == 1) {

                            for (Booking b : manager.allBookings()) {
                                System.out.println("");
                                System.out.println("    " + b);
                                System.out.println("        | Booking id: " + b.getId());
                                System.out.println("        | Guest id: " + b.getGuestId());
                                System.out.println("        | CheckIn time: " + b.getCheckIn());
                                System.out.println("        | CheckOut time: " + b.getCheckOut());
                                System.out.println("");
                            }

                        }
                        System.out.println("Press Enter to continue");
                        sc.nextLine();
                        sc.nextLine();
                    }
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid Option Selected");
                    System.out.println("Press Enter to continue");
                    sc.nextLine();
                    sc.nextLine();
                    break;
            }
        }
    }

    static LocalDate getdatefromuser(Scanner sc, String prompt) {
        while (true) {
            clear();
            System.out.println(prompt);
            try {
                System.out.println("Enter The Year");
                System.out.print('>');
                int year = sc.nextInt();
                System.out.println("Enter The Month");
                System.out.print('>');
                int month = sc.nextInt();
                System.out.println("Enter The Day of Month");
                System.out.print('>');
                int day = sc.nextInt();
                return LocalDate.of(year, month, day);
            } catch (DateTimeException e) {
                clear();
                System.out.println("Out of Range Date Values Provided :" + e);
                System.out.println("Press Enter to continue");
                sc.nextLine();
                sc.nextLine();
            }
        }
    }

    public static void clear() {
        // ANSI escape code to clear the screen and move cursor to top-left
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void test() {
        HotelManager manager = new HotelManager();
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
