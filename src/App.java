import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

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

    public ArrayList<Integer> getBookingIds() {
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

    public ArrayList<Room> listAvailableRoomsByType(ROOM_TYPE type, LocalDate start, LocalDate endExclusive) {
        ArrayList<Room> result = new ArrayList<>();
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

class MyButton extends JButton {
    Color currentcol;
    Color borderColor;

    MyButton(Color hoveColor,
            Color basecol,
            Color selectcol,
            Border paddingBorder,
            MatteBorder matteBorder) {
        this.currentcol = basecol;
        this.setBackground(currentcol);
        this.borderColor = matteBorder.getMatteColor();
        setBorder(new CompoundBorder(matteBorder, paddingBorder));
        setFocusPainted(false);
        this.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                currentcol = hoveColor;
                setBackground(currentcol);
            }

            public void mouseExited(MouseEvent e) {
                currentcol = basecol;
                setBackground(currentcol);
            }

        });
        this.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                MatteBorder border = new MatteBorder(matteBorder.getBorderInsets(), selectcol);
                setBorder(new CompoundBorder(border, paddingBorder));
            }

            public void focusLost(FocusEvent e) {
                MatteBorder border = new MatteBorder(matteBorder.getBorderInsets(), borderColor);
                setBorder(new CompoundBorder(border, paddingBorder));
            }
        });
    }

}

class Contentpanel {
    Cardpanel panel;
    Dataloader loader;
    JButton createButton;
    JButton deleteButton;
    JButton editButton;
    JButton refreshButton;
    int infopanelcurrent;

    Contentpanel(Cardpanel panel, Dataloader loader) {
        this.loader = loader;
        this.panel = panel;
        configuremainpanel();
        configureinfopanel();
        loader.renderall(panel.innerTiles);
        loader.getinfo(0, panel.infopanel);
    }

    private void configuremainpanel() {
        GridBagConstraints con = new GridBagConstraints();
        Color maincol = new Color(230, 230, 230);

        JPanel additionbuttons = new JPanel(new BorderLayout());
        additionbuttons.setPreferredSize(new Dimension(0, 60));
        additionbuttons.setMinimumSize(new Dimension(0, 60));
        additionbuttons.setMaximumSize(new Dimension(0, 60));
        additionbuttons.setBackground(maincol);
        Border lineBorder = new MatteBorder(3, 3, 0, 3, Color.GRAY);
        Border paddingBorder = new EmptyBorder(10, 10, 10, 10);
        additionbuttons.setBorder(new CompoundBorder(lineBorder, paddingBorder));

        createButton = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(10, 10, 10, 10),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        createButton.setText("Create New");
        createButton.setIcon(new ImageIcon("icons/list-add.png"));
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loader.insert();
                loader.renderall(panel.innerTiles);
                loader.getinfo(infopanelcurrent, panel.infopanel);
            }
        });
        additionbuttons.add(createButton, BorderLayout.EAST);

        refreshButton = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(10, 10, 10, 10),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        refreshButton.setText("Refresh");
        refreshButton.setIcon(new ImageIcon("icons/view-refresh.png"));
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loader.renderall(panel.innerTiles);
                loader.getinfo(infopanelcurrent, panel.infopanel);
            }
        });
        additionbuttons.add(refreshButton, BorderLayout.WEST);
        con.anchor = GridBagConstraints.NORTH;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 0;
        panel.mainpanel.add(additionbuttons, con);

        panel.innerTiles = new JPanel();
        panel.innerTiles.setLayout(new BoxLayout(panel.innerTiles, BoxLayout.Y_AXIS));
        panel.innerTiles.setBackground(maincol);
        panel.scrollPanemainpanel = new JScrollPane(panel.innerTiles, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.scrollPanemainpanel
                .setBorder(new CompoundBorder(new MatteBorder(0, 3, 3, 3, Color.GRAY), new EmptyBorder(2, 2, 2, 2)));
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.BOTH;
        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        panel.mainpanel.add(panel.scrollPanemainpanel, con);

    }

    private void configureinfopanel() {
        GridBagConstraints con = new GridBagConstraints();
        Color maincol = new Color(230, 230, 230);

        JPanel controlbuttons = new JPanel(new BorderLayout());
        controlbuttons.setPreferredSize(new Dimension(0, 60));
        controlbuttons.setMinimumSize(new Dimension(0, 60));
        controlbuttons.setMaximumSize(new Dimension(0, 60));
        controlbuttons.setBackground(maincol);
        Border lineBorder = new MatteBorder(3, 3, 0, 3, Color.GRAY);
        Border paddingBorder = new EmptyBorder(10, 10, 10, 10);
        controlbuttons.setBorder(new CompoundBorder(lineBorder, paddingBorder));

        editButton = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(10, 10, 10, 10),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        editButton.setText("Edit");
        editButton.setIcon(new ImageIcon("icons/document-edit.png"));
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loader.edit(infopanelcurrent);
                loader.renderall(panel.innerTiles);
                loader.getinfo(infopanelcurrent, panel.infopanel);
            }
        });
        controlbuttons.add(editButton, BorderLayout.EAST);

        deleteButton = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(10, 10, 10, 10),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        deleteButton.setText("Delete");
        deleteButton.setIcon(new ImageIcon("icons/edit-delete.png"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infopanelcurrent = loader.remove(infopanelcurrent);
                loader.renderall(panel.innerTiles);
                loader.getinfo(infopanelcurrent, panel.infopanel);
            }
        });
        controlbuttons.add(deleteButton, BorderLayout.WEST);
        con.anchor = GridBagConstraints.NORTH;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 0;
        panel.psideinfo.add(controlbuttons, con);

        panel.infopanel = new JPanel();
        panel.infopanel.setLayout(new BoxLayout(panel.infopanel, BoxLayout.Y_AXIS));
        panel.scrollPaneinfopanel = new JScrollPane(panel.infopanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.infopanel.setBackground(maincol);
        panel.scrollPaneinfopanel
                .setBorder(new CompoundBorder(new MatteBorder(0, 3, 3, 3, Color.GRAY), new EmptyBorder(2, 2, 2, 2)));

        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.BOTH;
        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        panel.psideinfo.add(panel.scrollPaneinfopanel, con);
    }
}

class Cardpanel {
    JPanel main;
    JPanel sidebar;
    JPanel mainpanel;
    JPanel psidenav;
    JPanel psideinfo;
    JScrollPane scrollPanemainpanel;
    JScrollPane scrollPaneinfopanel;
    JPanel infopanel;
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
        con.gridwidth = 1;
        con.weightx = 3.0;
        con.weighty = 1.0;
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
            Color basecol;
            if (i == pan) {
                basecol = selectcol;
            } else {
                basecol = maincol;
            }
            JButton button = new MyButton(hoverColor,
                    basecol,
                    Color.BLUE,
                    new EmptyBorder(1, 1, 1, 0),
                    new MatteBorder(3, 3, 3, 3, Color.GRAY));
            JPanel panel = new JPanel(new BorderLayout());
            JLabel iconLabel = new JLabel(new ImageIcon(images[i]));
            JLabel textLabel = new JLabel(buttonLabels[i], SwingConstants.CENTER);
            panel.setOpaque(false);
            button.setLayout(new BorderLayout());
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(textLabel, BorderLayout.CENTER);
            button.setActionCommand(buttonLabels[i]);
            button.add(panel);
            button.addActionListener(navlist);
            constr.gridy = i + 1;
            navPanel.add(button, constr);
        }

        return navPanel;
    }

    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setBackground(Color.LIGHT_GRAY);

        Border lineBorder = new MatteBorder(3, 3, 3, 3, Color.GRAY);
        Border paddingBorder = new EmptyBorder(10, 10, 10, 10);
        controlsPanel.setBorder(new CompoundBorder(lineBorder, paddingBorder));

        return controlsPanel;
    }

}

interface Dataloader {
    void insert();

    int remove(int item);

    void edit(int item);

    void getinfo(int item, JPanel panel);

    void renderall(JPanel panel);
}

class Roompanel implements Dataloader {
    JFrame frame;

    Roompanel(JFrame frame) {
        this.frame = frame;
    }

    public void insert() {
        JDialog dialog = new JDialog(frame, "Create New Room");
        dialog.setSize(500, 300);

        dialog.setVisible(true);
    }

    public int remove(int item) {
        JOptionPane.showConfirmDialog(frame, "are you sure you want to delete this Room", "confirm deletion",
                JOptionPane.YES_NO_OPTION);
        return 0;
    }

    public void edit(int item) {
        JDialog dialog = new JDialog(frame, "Edit Room");
        dialog.setSize(500, 300);

        dialog.setVisible(true);
    }

    public void getinfo(int item, JPanel panel) {

    }

    public void renderall(JPanel panel) {

    }
}

class Guestpanel implements Dataloader {
    JFrame frame;

    Guestpanel(JFrame frame) {
        this.frame = frame;
    }

    public void insert() {
        JDialog dialog = new JDialog(frame, "Create New Guest");
        dialog.setSize(500, 300);

        dialog.setVisible(true);
    }

    public int remove(int item) {
        JOptionPane.showConfirmDialog(frame, "are you sure you want to delete this Guest", "confirm deletion",
                JOptionPane.YES_NO_OPTION);
        return 0;
    }

    public void edit(int item) {
        JDialog dialog = new JDialog(frame, "Edit Guest Details");
        dialog.setSize(500, 300);

        dialog.setVisible(true);
    }

    public void getinfo(int item, JPanel panel) {
    }

    public void renderall(JPanel panel) {
    }
}

class Bookingpanel implements Dataloader {
    JFrame frame;

    Bookingpanel(JFrame frame) {
        this.frame = frame;
    }

    public void insert() {
        JDialog dialog = new JDialog(frame, "Create New Booking");
        dialog.setSize(500, 300);

        dialog.setVisible(true);
    }

    public int remove(int item) {
        JOptionPane.showConfirmDialog(frame, "are you sure you want to delete this booking", "confirm deletion",
                JOptionPane.YES_NO_OPTION);
        return 0;
    }

    public void edit(int item) {
        JDialog dialog = new JDialog(frame, "Edit Booking Details");
        dialog.setSize(500, 300);

        dialog.setVisible(true);
    }

    public void getinfo(int item, JPanel panel) {
    }

    public void renderall(JPanel panel) {
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

    Contentpanel room;
    Contentpanel guest;
    Contentpanel booking;
    CardLayout cardlayout;
    Connection conn;

    void start() {
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
        room = new Contentpanel(roommgmt, new Roompanel(frame));
        frame.add(roommgmt.main, "room");
        guestmgmt = new Cardpanel(2, navlist);
        guest = new Contentpanel(guestmgmt, new Guestpanel(frame));
        frame.add(guestmgmt.main, "guest");
        bookingmgmt = new Cardpanel(3, navlist);
        booking = new Contentpanel(bookingmgmt, new Bookingpanel(frame));
        frame.add(bookingmgmt.main, "booking");

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Database.close();
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

}

class Database {
    static Connection dbconn = null;

    static void close() {
        try {
            dbconn.close();
            System.out.println("connection closed");
        } catch (Exception ex) {
            System.out.println("connection unable to close");
        }
    }

    static void connectdbprompt(AppUI UI) {
        JFrame dialog = new JFrame();
        dialog.setSize(new Dimension(400, 300));
        dialog.setResizable(false);
        ;
        dialog.setTitle("Connect to database");
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.fill = GridBagConstraints.HORIZONTAL;
        constr.insets = new Insets(3, 6, 3, 6);

        JLabel labip = new JLabel("Database IP:");
        constr.gridx = 0;
        constr.gridy = 0;
        constr.gridwidth = 1;
        constr.gridheight = 1;
        constr.weightx = 1;
        constr.weighty = 1;
        dialog.add(labip, constr);
        JTextField ip = new JTextField("192.168.122.42", 16);
        constr.gridx = 1;
        constr.gridy = 0;
        constr.gridwidth = 4;
        constr.gridheight = 1;
        constr.weightx = 4;
        constr.weighty = 1;
        dialog.add(ip, constr);

        JLabel labport = new JLabel("Port:");
        constr.gridx = 5;
        constr.gridy = 0;
        constr.gridwidth = 1;
        constr.gridheight = 1;
        constr.weightx = 4;
        constr.weighty = 1;
        dialog.add(labport, constr);
        JTextField port = new JTextField("3306", 5);
        constr.gridx = 6;
        constr.gridy = 0;
        constr.gridwidth = 1;
        constr.gridheight = 1;
        constr.weightx = 1;
        constr.weighty = 1;
        dialog.add(port, constr);
        JTextField username = new JTextField("javahotel", 16);

        JLabel labusr = new JLabel("User Name:");
        constr.gridx = 0;
        constr.gridy = 1;
        constr.gridwidth = 1;
        constr.gridheight = 1;
        constr.weightx = 1;
        constr.weighty = 1;
        dialog.add(labusr, constr);
        constr.gridx = 1;
        constr.gridy = 1;
        constr.gridwidth = 4;
        constr.gridheight = 1;
        constr.weightx = 1;
        constr.weighty = 1;
        dialog.add(username, constr);
        JTextField pass = new JTextField("admin", 16);

        JLabel labpass = new JLabel("Password:");
        constr.gridx = 0;
        constr.gridy = 2;
        constr.gridwidth = 1;
        constr.gridheight = 1;
        constr.weightx = 1;
        constr.weighty = 1;
        dialog.add(labpass, constr);
        constr.gridx = 1;
        constr.gridy = 2;
        constr.gridwidth = 4;
        constr.gridheight = 1;
        constr.weightx = 1;
        constr.weighty = 1;
        dialog.add(pass, constr);

        JButton button = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(10, 10, 10, 10),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        button.setText("Connect");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Database.dbconn = DriverManager.getConnection(
                            "jdbc:mysql://" + ip.getText() + ":" + port.getText() + "/javahotel", username.getText(),
                            pass.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                            ex,
                            "Database Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(dialog,
                        "DataBase Connected Sucessfully",
                        "Database Connection Status",
                        JOptionPane.INFORMATION_MESSAGE);
                inittables(dialog);
                UI.start();
                dialog.dispose();
            }
        });
        constr.gridx = 1;
        constr.gridy = 3;
        constr.gridwidth = 4;
        constr.gridheight = 1;
        constr.weightx = 4;
        constr.weighty = 1;
        dialog.add(button, constr);
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dialog.setVisible(true);
    }

    static void inittables(JFrame dialog) {
        try {
            Statement stmt = dbconn.createStatement();
            String[] tables = {
                    "CREATE TABLE IF NOT EXISTS guest (guestid INT NOT NULL auto_increment PRIMARY KEY,name TEXT NOT NULL,phone TEXT NOT NULL,email TEXT NOT NULL);",
                    "CREATE TABLE IF NOT EXISTS room (roomid INT NOT NULL auto_increment PRIMARY KEY,room_type INT NOT NULL,price DOUBLE NOT NULL);",
                    "CREATE TABLE IF NOT EXISTS booking ( bookingid INT NOT NULL auto_increment PRIMARY KEY,checkin DATE,checkout DATE,roomid INT NOT NULL,guestid INT NOT NULL,CONSTRAINT fk_roomid FOREIGN KEY (roomid) REFERENCES room(roomid),CONSTRAINT fk_guestid FOREIGN KEY (guestid) REFERENCES guest(guestid));" };
            for (int i = 0; i < tables.length; i++) {
                stmt.executeUpdate(tables[i]);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}

public class App {
    public static void main(String[] args) {
        AppUI UI = new AppUI();
        UIManager.put("OptionPane.background", new Color(230, 230, 230));
        UIManager.put("Panel.background", new Color(230, 230, 230));
        Database.connectdbprompt(UI);

    }
}
