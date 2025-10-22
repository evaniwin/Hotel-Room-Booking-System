import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.*;

import com.mysql.cj.protocol.Resultset;

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

    public static String strfromint(int val) {
        switch (val) {
            case 1:
                return "Single";
            case 2:
                return "Double";
            case 3:
                return "Deluxe";
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
        loader.renderall(panel.innerTiles, this);
        loader.getinfo(1, panel.infopanel);
        infopanelcurrent = 1;
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
        Contentpanel self = this;
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loader.insert();
                loader.renderall(panel.innerTiles, self);
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
                loader.getinfo(infopanelcurrent, panel.infopanel);
                loader.renderall(panel.innerTiles, self);
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
        panel.innerTiles.setLayout(new GridBagLayout());
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
        Contentpanel self = this;
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loader.edit(infopanelcurrent);
                loader.renderall(panel.innerTiles, self);
                loader.getinfo(infopanelcurrent, panel.infopanel);
            }
        });
        controlbuttons.add(editButton, BorderLayout.EAST);
        JLabel lab1 = new JLabel("Details");
        lab1.setHorizontalAlignment(SwingConstants.CENTER);
        controlbuttons.add(lab1, BorderLayout.CENTER);

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
                loader.renderall(panel.innerTiles, self);
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
        panel.infopanel.setLayout(new GridBagLayout());
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

    void renderall(JPanel panel, Contentpanel cp);
}

class Roompanel implements Dataloader {
    JFrame frame;
    PreparedStatement insertentry;
    PreparedStatement getentry;
    PreparedStatement editentry;
    PreparedStatement delete_entry;
    PreparedStatement getallentry;
    PreparedStatement getrelatedbookings;
    PreparedStatement findnearestid;

    Roompanel(JFrame frame) {
        this.frame = frame;
        try {
            insertentry = Database.dbconn.prepareStatement("INSERT INTO room (room_type,price) VALUES (?,?);");
            getentry = Database.dbconn.prepareStatement("SELECT * FROM room  WHERE roomid = ?;");
            editentry = Database.dbconn.prepareStatement("UPDATE room SET room_type = ?, price = ? WHERE roomid = ?;");
            delete_entry = Database.dbconn.prepareStatement("DELETE FROM room WHERE roomid = ?;");
            getallentry = Database.dbconn.prepareStatement("SELECT * FROM room;");
            getrelatedbookings = Database.dbconn.prepareStatement(
                    "SELECT booking.* FROM room INNER JOIN booking ON room.roomid = booking.roomid WHERE room.roomid = ?;");
            findnearestid = Database.dbconn
                    .prepareStatement("SELECT * FROM room ORDER BY ABS(room.roomid - ?) LIMIT 1;");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void insert() {
        JDialog dialog = new JDialog(frame, "Create New Room");
        dialog.setSize(500, 200);
        dialog.setResizable(false);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.BOTH;

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JLabel lab1 = new JLabel("      Room Type: ");
        dialog.add(lab1, con);

        JRadioButton rd1 = new JRadioButton("Single", true);
        rd1.setActionCommand("1");
        JRadioButton rd2 = new JRadioButton("Double", false);
        rd2.setActionCommand("2");
        JRadioButton rd3 = new JRadioButton("Deluxe", false);
        rd3.setActionCommand("3");
        ButtonGroup bgr = new ButtonGroup();
        con.gridx = 1;
        bgr.add(rd1);
        dialog.add(rd1, con);
        con.gridy = 1;
        bgr.add(rd2);
        dialog.add(rd2, con);
        con.gridy = 2;
        bgr.add(rd3);
        dialog.add(rd3, con);

        con.gridx = 0;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JLabel lab2 = new JLabel("      Room Price per Night: ");
        lab2.setBorder(new CompoundBorder(new MatteBorder(3, 0, 3, 0, Color.GRAY), new EmptyBorder(5, 0, 5, 0)));
        dialog.add(lab2, con);

        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JSpinner spi = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 100));
        dialog.add(spi, con);
        spi.setBorder(new CompoundBorder(new MatteBorder(3, 0, 3, 0, Color.GRAY), new EmptyBorder(5, 0, 5, 0)));
        con.gridy = 4;
        JButton btn = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(5, 5, 5, 5),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        btn.setText("Create");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int roomtype = Integer.parseInt(bgr.getSelection().getActionCommand());
                int price = (int) spi.getValue();
                try {
                    insertentry.setInt(1, roomtype);
                    insertentry.setInt(2, price);
                    insertentry.execute();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            ex,
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    dialog.dispose();
                }
            }
        });
        dialog.add(btn, con);
        dialog.setVisible(true);

    }

    public int remove(int item) {
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Room " + item + " does not exist",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return item;
            }
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to Delete Room Number '" + item + "'",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                delete_entry.setInt(1, item);
                delete_entry.execute();
                findnearestid.setInt(1, item);
                ResultSet rs = findnearestid.executeQuery();
                rs.next();
                return rs.getInt("roomid");

            }
            return item;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return item;
        }

    }

    public void edit(int item) {

        int roomtype;
        int price;
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Room" + item + " does not exist",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            roomtype = rslt.getInt("room_type");
            price = rslt.getInt("price");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog(frame, "Edit Room");
        dialog.setSize(500, 200);
        dialog.setResizable(false);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.BOTH;

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JLabel lab1 = new JLabel("      Room Type: ");
        dialog.add(lab1, con);

        JRadioButton rd1 = new JRadioButton("Single", roomtype == 1);
        rd1.setActionCommand("1");
        JRadioButton rd2 = new JRadioButton("Double", roomtype == 2);
        rd2.setActionCommand("2");
        JRadioButton rd3 = new JRadioButton("Deluxe", roomtype == 3);
        rd3.setActionCommand("3");
        ButtonGroup bgr = new ButtonGroup();
        con.gridx = 1;
        bgr.add(rd1);
        dialog.add(rd1, con);
        con.gridy = 1;
        bgr.add(rd2);
        dialog.add(rd2, con);
        con.gridy = 2;
        bgr.add(rd3);
        dialog.add(rd3, con);

        con.gridx = 0;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JLabel lab2 = new JLabel("      Room Price per Night: ");
        lab2.setBorder(new CompoundBorder(new MatteBorder(3, 0, 3, 0, Color.GRAY), new EmptyBorder(5, 0, 5, 0)));
        dialog.add(lab2, con);

        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JSpinner spi = new JSpinner(new SpinnerNumberModel(price, 0, Integer.MAX_VALUE, 100));
        dialog.add(spi, con);
        spi.setBorder(new CompoundBorder(new MatteBorder(3, 0, 3, 0, Color.GRAY), new EmptyBorder(5, 0, 5, 0)));
        con.gridy = 4;
        JButton btn = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(5, 5, 5, 5),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        btn.setText("Create");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int roomtype = Integer.parseInt(bgr.getSelection().getActionCommand());
                int price = (int) spi.getValue();
                try {
                    editentry.setInt(1, roomtype);
                    editentry.setInt(2, price);
                    editentry.setInt(3, item);
                    editentry.execute();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            ex,
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    dialog.dispose();
                }
            }
        });
        dialog.add(btn, con);
        dialog.setVisible(true);
    }

    public void getinfo(int item, JPanel panel) {

        int roomtype;
        int price;
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Room" + item + " does not exist",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            roomtype = rslt.getInt("room_type");
            price = rslt.getInt("price");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        JLabel lab1 = new JLabel("Room Number :");
        JLabel lab2 = new JLabel("Room Type :");
        JLabel lab3 = new JLabel("Price Per Night :");
        JTextField fl1 = new JTextField(String.valueOf(item));
        fl1.setEditable(false);
        JTextField fl2 = new JTextField(ROOM_TYPE.strfromint(roomtype));
        fl2.setEditable(false);
        JTextField fl3 = new JTextField(String.valueOf(price));
        fl3.setEditable(false);

        panel.removeAll();
        panel.revalidate();
        panel.repaint();

        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new Insets(5, 5, 5, 5);

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        panel.add(lab1, con);
        con.gridy = 1;
        panel.add(lab2, con);
        con.gridy = 2;
        panel.add(lab3, con);

        con.gridx = 1;
        con.gridy = 0;
        panel.add(fl1, con);
        con.gridy = 1;
        panel.add(fl2, con);
        con.gridy = 2;
        panel.add(fl3, con);

        con.gridx = 0;
        con.gridy = 3;
        con.fill = GridBagConstraints.BOTH;
        con.gridwidth = 2;
        con.weighty = 1000;
        panel.add(new JPanel(), con);
    }

    // Todo
    public void renderall(JPanel panel, Contentpanel cp) {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
        int i = 0;
        GridBagConstraints con = new GridBagConstraints();
        try {
            ResultSet rslt = getallentry.executeQuery();
            while (rslt.next()) {
                int roomid = rslt.getInt("roomid");
                int roomtype = rslt.getInt("room_type");
                int price = rslt.getInt("price");
                JButton btn = new MyButton(new Color(130, 180, 190),
                        new Color(160, 160, 160),
                        Color.blue,
                        new EmptyBorder(5, 5, 5, 5),
                        new MatteBorder(3, 3, 3, 3, Color.GRAY));
                con.anchor = GridBagConstraints.CENTER;
                con.fill = GridBagConstraints.HORIZONTAL;
                con.gridx = 0;
                con.gridy = i;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = 1;
                con.weighty = 0;
                panel.add(btn, con);
                JPanel tile = new JPanel(new GridBagLayout());
                btn.add(tile);
                con.gridx = 0;
                con.gridy = 0;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = 1;
                con.weighty = 0;
                JLabel lab1 = new JLabel("Room Number: " + roomid);
                tile.add(lab1, con);
                con.gridx = 1;
                JLabel lab2 = new JLabel("Room Type : " + ROOM_TYPE.strfromint(roomtype));
                tile.add(lab2, con);
                con.gridx = 2;
                JLabel lab3 = new JLabel("Price: " + price);
                tile.add(lab3, con);
                Dataloader self = this;
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ex) {
                        self.getinfo(roomid, cp.panel.infopanel);
                        cp.infopanelcurrent = roomid;
                    }
                });

                i++;
            }
            con.anchor = GridBagConstraints.CENTER;
            con.fill = GridBagConstraints.VERTICAL;
            con.gridx = 0;
            con.gridy = i;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.weightx = 1;
            con.weighty = 1;
            panel.add(new JPanel(), con);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
}

class Guestpanel implements Dataloader {
    JFrame frame;
    PreparedStatement insertentry;
    PreparedStatement getentry;
    PreparedStatement editentry;
    PreparedStatement delete_entry;
    PreparedStatement getallentry;
    PreparedStatement getrelatedbookings;
    PreparedStatement findnearestid;

    Guestpanel(JFrame frame) {
        this.frame = frame;
        try {
            insertentry = Database.dbconn.prepareStatement("INSERT INTO guest (name,phone,email) VALUES (?,?,?);");
            getentry = Database.dbconn.prepareStatement("SELECT * FROM guest WHERE guestid = ?;");
            editentry = Database.dbconn
                    .prepareStatement("UPDATE guest SET name = ?,phone = ?, email = ? WHERE guestid = ?;");
            delete_entry = Database.dbconn.prepareStatement("DELETE FROM guest WHERE guestid = ?;");
            getallentry = Database.dbconn.prepareStatement("SELECT * FROM guest;");
            getrelatedbookings = Database.dbconn.prepareStatement(
                    "SELECT booking.* FROM guest INNER JOIN booking ON guest.guestid = booking.guestid WHERE guest.guestid = ?;");
            findnearestid = Database.dbconn
                    .prepareStatement("SELECT * FROM guest ORDER BY ABS(guest.guestid - ?) LIMIT 1;");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void insert() {

        JDialog dialog = new JDialog(frame, "Create New Room");
        dialog.setSize(500, 200);
        dialog.setResizable(false);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.HORIZONTAL;

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;

        JLabel lab1 = new JLabel("      Guest Name: ");
        JLabel lab2 = new JLabel("      Guest Phone Number:  ");
        JLabel lab3 = new JLabel("      Guest Email: ");

        con.gridy = 0;
        dialog.add(lab1, con);
        con.gridy = 1;
        dialog.add(lab2, con);
        con.gridy = 2;
        dialog.add(lab3, con);

        JTextField tf1 = new JTextField(30);
        JTextField tf2 = new JTextField(30);
        JTextField tf3 = new JTextField(30);

        con.gridx = 1;
        con.gridy = 0;
        dialog.add(tf1, con);
        con.gridy = 1;
        dialog.add(tf2, con);
        con.gridy = 2;
        dialog.add(tf3, con);

        JButton btn = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(5, 5, 5, 5),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        btn.setText("Create");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    insertentry.setString(1, tf1.getText());
                    insertentry.setString(2, tf2.getText());
                    insertentry.setString(3, tf3.getText());
                    insertentry.execute();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            ex,
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    dialog.dispose();
                }
            }
        });
        con.gridx = 1;
        con.gridy = 3;
        dialog.add(btn, con);
        con.gridy = 0;
        con.gridx = 2;
        JPanel pan = new JPanel();
        Dimension dim = new Dimension(8, 8);
        pan.setMinimumSize(dim);
        pan.setMaximumSize(dim);
        pan.setPreferredSize(dim);
        dialog.add(pan, con);
        dialog.setVisible(true);
    }

    public int remove(int item) {
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Guest " + item + " does not exist",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return item;
            }
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to Delete Guest Number '" + item + "'",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                delete_entry.setInt(1, item);
                delete_entry.execute();
                findnearestid.setInt(1, item);
                ResultSet rs = findnearestid.executeQuery();
                rs.next();
                return rs.getInt("roomid");

            }
            return item;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return item;
        }
    }

    public void edit(int item) {
        String guestname;
        String guestphone;
        String guestemail;
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Guest Number" + item + " does not exist",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            guestname = rslt.getString("name");
            guestphone = rslt.getString("phone");
            guestemail = rslt.getString("email");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog(frame, "Create New Room");
        dialog.setSize(500, 200);
        dialog.setResizable(false);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.HORIZONTAL;

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;

        JLabel lab1 = new JLabel("      Guest Name: ");
        JLabel lab2 = new JLabel("      Guest Phone Number:  ");
        JLabel lab3 = new JLabel("      Guest Email: ");

        con.gridy = 0;
        dialog.add(lab1, con);
        con.gridy = 1;
        dialog.add(lab2, con);
        con.gridy = 2;
        dialog.add(lab3, con);

        JTextField tf1 = new JTextField(guestname, 30);
        JTextField tf2 = new JTextField(guestphone, 30);
        JTextField tf3 = new JTextField(guestemail, 30);

        con.gridx = 1;
        con.gridy = 0;
        dialog.add(tf1, con);
        con.gridy = 1;
        dialog.add(tf2, con);
        con.gridy = 2;
        dialog.add(tf3, con);

        JButton btn = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(5, 5, 5, 5),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        btn.setText("Create");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    editentry.setString(1, tf1.getText());
                    editentry.setString(2, tf2.getText());
                    editentry.setString(3, tf3.getText());
                    editentry.setInt(4, item);
                    insertentry.execute();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            ex,
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    dialog.dispose();
                }
            }
        });
        con.gridx = 1;
        con.gridy = 3;
        dialog.add(btn, con);
        con.gridy = 0;
        con.gridx = 2;
        JPanel pan = new JPanel();
        Dimension dim = new Dimension(8, 8);
        pan.setMinimumSize(dim);
        pan.setMaximumSize(dim);
        pan.setPreferredSize(dim);
        dialog.add(pan, con);
        dialog.setVisible(true);
    }

    public void getinfo(int item, JPanel panel) {
        String name;
        String phone;
        String email;
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Guest Number" + item + " does not exist",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            name = rslt.getString("name");
            phone = rslt.getString("phone");
            email = rslt.getString("email");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        JLabel lab1 = new JLabel("Guest Number :");
        JLabel lab2 = new JLabel("Guest Name :");
        JLabel lab3 = new JLabel("Guest Phone Number :");
        JLabel lab4 = new JLabel("Guest Email :");

        JTextField fl1 = new JTextField(String.valueOf(item));
        fl1.setEditable(false);
        JTextField fl2 = new JTextField(name);
        fl2.setEditable(false);
        JTextField fl3 = new JTextField(phone);
        fl3.setEditable(false);
        JTextField fl4 = new JTextField(email);
        fl4.setEditable(false);

        panel.removeAll();

        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new Insets(5, 5, 5, 5);

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        panel.add(lab1, con);
        con.gridy = 1;
        panel.add(lab2, con);
        con.gridy = 2;
        panel.add(lab3, con);
        con.gridy = 3;
        panel.add(lab4, con);

        con.gridx = 1;
        con.gridy = 0;
        panel.add(fl1, con);
        con.gridy = 1;
        panel.add(fl2, con);
        con.gridy = 2;
        panel.add(fl3, con);
        con.gridy = 3;
        panel.add(fl4, con);

        con.gridx = 0;
        con.gridy = 4;
        con.fill = GridBagConstraints.BOTH;
        con.gridwidth = 2;
        con.weighty = 1000;
        panel.add(new JPanel(), con);
    }

    public void renderall(JPanel panel, Contentpanel cp) {
        int guestid;
        String name;
        String phone;
        String email;
        try {
            ResultSet rslt = getallentry.executeQuery();
            while (rslt.next()) {
                guestid = rslt.getInt("guestid");
                name = rslt.getString("name");
                phone = rslt.getString("phone");
                email = rslt.getString("email");

            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
}

class Bookingpanel implements Dataloader {
    JFrame frame;
    PreparedStatement insertentry;
    PreparedStatement getentry;
    PreparedStatement editentry;
    PreparedStatement delete_entry;
    PreparedStatement getallentry;
    PreparedStatement checkexistroom;
    PreparedStatement checkexistguest;
    PreparedStatement get_room;
    PreparedStatement get_guest;

    Bookingpanel(JFrame frame) {
        this.frame = frame;
        try {
            insertentry = Database.dbconn
                    .prepareStatement("INSERT INTO booking (checkin,checkout,roomid,guestid) VALUES (?,?,?,?);");
            getentry = Database.dbconn.prepareStatement("SELECT * FROM booking  WHERE bookingid = ?;");
            editentry = Database.dbconn.prepareStatement(
                    "UPDATE booking SET checkin = ?, checkout = ?, roomid = ?, guestid = ? WHERE bookingid = ?;");
            delete_entry = Database.dbconn.prepareStatement("DELETE FROM booking WHERE bookingid = ?;");
            getallentry = Database.dbconn.prepareStatement("SELECT * FROM booking;");
            checkexistroom = Database.dbconn.prepareStatement("SELECT * FROM room WHERE roomid = ?;");
            checkexistguest = Database.dbconn.prepareStatement("SELECT * FROM guest WHERE guestid = ?;");
            get_guest = Database.dbconn.prepareStatement("SELECT * FROM guest;");
            get_room = Database.dbconn.prepareStatement("SELECT * FROM room;");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    Vector<Integer> roomids() {
        Vector<Integer> ids = new Vector<Integer>();
        try {
            ResultSet rslt = get_room.executeQuery();
            while (rslt.next()) {
                ids.add(rslt.getInt("roomid"));
            }
            return ids;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    Vector<Integer> guestids() {
        Vector<Integer> ids = new Vector<Integer>();
        try {
            ResultSet rslt = get_guest.executeQuery();
            while (rslt.next()) {
                ids.add(rslt.getInt("guestid"));
            }
            return ids;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public void insert() {
        JDialog dialog = new JDialog(frame, "Create New Booking");
        dialog.setSize(500, 300);
        dialog.setResizable(false);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.CENTER;
        con.fill = GridBagConstraints.BOTH;

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JLabel lab1 = new JLabel("      Checkin Date: ");
        JLabel lab2 = new JLabel("      Checkout Date: ");
        JLabel lab3 = new JLabel("      Room Number: ");
        JLabel lab4 = new JLabel("      Guest Number: ");

        dialog.add(lab1, con);
        con.gridy = 3;
        dialog.add(lab2, con);
        con.gridy = 6;
        dialog.add(lab3, con);
        con.gridy = 7;
        dialog.add(lab4, con);

        con.gridx = 1;
        con.gridy = 0;
        JSpinner spi1 = new JSpinner(new SpinnerDateModel(new Date(2025, 1, 1), null, null, Calendar.YEAR));
        dialog.add(spi1, con);

        con.gridy = 1;
        JSpinner spi4 = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        dialog.add(spi4, con);

        con.gridy = 2;
        JComboBox<Integer> cmb1 = new JComboBox<Integer>(roomids());
        dialog.add(cmb1, con);
        con.gridy = 3;
        JComboBox<Integer> cmb2 = new JComboBox<Integer>(guestids());
        dialog.add(cmb2, con);

        con.gridy = 4;
        JButton btn = new MyButton(new Color(130, 180, 190),
                new Color(160, 160, 160),
                Color.blue,
                new EmptyBorder(5, 5, 5, 5),
                new MatteBorder(3, 3, 3, 3, Color.GRAY));
        btn.setText("Create");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            ex,
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    dialog.dispose();
                }
            }
        });
        dialog.add(btn, con);
        dialog.setVisible(true);

    }

    public int remove(int item) {
        JOptionPane.showConfirmDialog(frame, "Are you sure you want to Delete Booking Number '" + item + "'",
                "Confirm Deletion",
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

    public void renderall(JPanel panel, Contentpanel cp) {
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
