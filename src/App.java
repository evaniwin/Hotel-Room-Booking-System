import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
    Hotelmanager loader;
    JButton createButton;
    JButton deleteButton;
    JButton editButton;
    JButton refreshButton;
    int infopanelcurrent;

    Contentpanel(Cardpanel panel, Hotelmanager loader) {
        this.loader = loader;
        this.panel = panel;
        configuremainpanel();
        configureinfopanel();
        loader.renderall(panel.innerTiles, this);
        infopanelcurrent = loader.getinfo(1, panel.infopanel);
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
                infopanelcurrent = loader.getinfo(infopanelcurrent, panel.infopanel);
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
                infopanelcurrent = loader.getinfo(infopanelcurrent, panel.infopanel);
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
                infopanelcurrent = loader.getinfo(infopanelcurrent, panel.infopanel);
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
                infopanelcurrent = loader.getinfo(infopanelcurrent, panel.infopanel);
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

interface Hotelmanager {
    void insert();

    int remove(int item);

    void edit(int item);

    int getinfo(int item, JPanel panel);

    void renderall(JPanel panel, Contentpanel cp);
}

class Room implements Hotelmanager {
    JFrame frame;
    PreparedStatement insertentry;
    PreparedStatement getentry;
    PreparedStatement editentry;
    PreparedStatement delete_entry;
    PreparedStatement getallentry;
    PreparedStatement delrelatedbookings;
    PreparedStatement getrelatedbookings;
    PreparedStatement findnearestid;

    Room(JFrame frame) {
        this.frame = frame;
        try {
            insertentry = Database.dbconn.prepareStatement("INSERT INTO room (room_type,price) VALUES (?,?);");
            getentry = Database.dbconn.prepareStatement("SELECT * FROM room  WHERE roomid = ?;");
            editentry = Database.dbconn.prepareStatement("UPDATE room SET room_type = ?, price = ? WHERE roomid = ?;");
            delete_entry = Database.dbconn.prepareStatement("DELETE FROM room WHERE roomid = ?;");
            getallentry = Database.dbconn.prepareStatement("SELECT * FROM room;");
            delrelatedbookings = Database.dbconn.prepareStatement(
                    "DELETE FROM booking where roomid = ?;");
            getrelatedbookings = Database.dbconn.prepareStatement(
                    "SELECT * FROM booking where roomid = ?;");
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

        con.insets = new Insets(4, 4, 4, 4);
        con.gridx = 0;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JLabel lab2 = new JLabel("      Room Price per Night: ");
        dialog.add(lab2, con);

        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JSpinner spi = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 100));
        dialog.add(spi, con);
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
        if (item == -1) {
            return item;
        }
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
                delrelatedbookings.setInt(1, item);
                delrelatedbookings.execute();
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
        if (item == -1) {
            return;
        }
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
        con.insets = new Insets(4, 4, 4, 4);
        con.gridx = 0;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JLabel lab2 = new JLabel("      Room Price per Night: ");

        dialog.add(lab2, con);

        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;
        JSpinner spi = new JSpinner(new SpinnerNumberModel(price, 0, Integer.MAX_VALUE, 100));
        dialog.add(spi, con);
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

    public int getinfo(int item, JPanel panel) {

        int roomtype;
        int price;
        try {
            if (item == -1) {
                findnearestid.setInt(1, item);
                ResultSet rslt = findnearestid.executeQuery();
                if (rslt.next()) {
                    item = rslt.getInt("roomid");
                } else {
                    return -1;
                }
            }
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                // JOptionPane.showMessageDialog(frame,
                // "Room" + item + " does not exist",
                // "Database Error",
                // JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            roomtype = rslt.getInt("room_type");
            price = rslt.getInt("price");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return item;
        }
        JLabel lab1 = new JLabel("Room Number :");
        JLabel lab2 = new JLabel("Room Type :");
        JLabel lab3 = new JLabel("Price Per Night :");
        JTextField fl1 = new JTextField(String.valueOf(item), 10);
        fl1.setEditable(false);
        JTextField fl2 = new JTextField(ROOM_TYPE.strfromint(roomtype), 10);
        fl2.setEditable(false);
        JTextField fl3 = new JTextField(String.valueOf(price), 10);
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
        con.gridy = 4;

        con.fill = GridBagConstraints.BOTH;
        con.gridwidth = 2;
        con.weighty = 1000;
        panel.add(new JPanel(), con);
        return item;
    }

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
                Hotelmanager self = this;
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ex) {
                        cp.infopanelcurrent = self.getinfo(roomid, cp.panel.infopanel);
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

class Guest implements Hotelmanager {
    JFrame frame;
    PreparedStatement insertentry;
    PreparedStatement getentry;
    PreparedStatement editentry;
    PreparedStatement delete_entry;
    PreparedStatement getallentry;
    PreparedStatement delrelatedbookings;
    PreparedStatement findnearestid;

    Guest(JFrame frame) {
        this.frame = frame;
        try {
            insertentry = Database.dbconn.prepareStatement("INSERT INTO guest (name,phone,email) VALUES (?,?,?);");
            getentry = Database.dbconn.prepareStatement("SELECT * FROM guest WHERE guestid = ?;");
            editentry = Database.dbconn
                    .prepareStatement("UPDATE guest SET name = ?,phone = ?, email = ? WHERE guestid = ?;");
            delete_entry = Database.dbconn.prepareStatement("DELETE FROM guest WHERE guestid = ?;");
            getallentry = Database.dbconn.prepareStatement("SELECT * FROM guest;");
            delrelatedbookings = Database.dbconn.prepareStatement(
                    "DELETE FROM booking where guestid = ?;");
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
        con.fill = GridBagConstraints.BOTH;

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;

        JLabel lab1 = new JLabel("      Guest Name: ");
        JLabel lab2 = new JLabel("      Guest Phone Number:  ");
        JLabel lab3 = new JLabel("      Guest Email: ");
        con.insets = new Insets(4, 4, 4, 0);
        con.gridy = 0;
        dialog.add(lab1, con);
        con.gridy = 1;
        dialog.add(lab2, con);
        con.gridy = 2;
        dialog.add(lab3, con);
        con.insets = new Insets(4, 0, 4, 4);
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
        dialog.setVisible(true);
    }

    public int remove(int item) {
        if (item == -1) {
            return item;
        }
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
                delrelatedbookings.setInt(1, item);
                delrelatedbookings.execute();
                delete_entry.setInt(1, item);
                delete_entry.execute();
                findnearestid.setInt(1, item);
                ResultSet rs = findnearestid.executeQuery();
                rs.next();
                return rs.getInt("guestid");

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
        if (item == -1) {
            return;
        }
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
        con.fill = GridBagConstraints.BOTH;

        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 1;

        JLabel lab1 = new JLabel("      Guest Name: ");
        JLabel lab2 = new JLabel("      Guest Phone Number:  ");
        JLabel lab3 = new JLabel("      Guest Email: ");
        con.insets = new Insets(4, 4, 4, 0);
        con.gridy = 0;
        dialog.add(lab1, con);
        con.gridy = 1;
        dialog.add(lab2, con);
        con.gridy = 2;
        dialog.add(lab3, con);
        con.insets = new Insets(4, 0, 4, 4);
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
        con.gridx = 1;
        con.gridy = 3;
        dialog.add(btn, con);
        dialog.setVisible(true);
    }

    public int getinfo(int item, JPanel panel) {
        String name;
        String phone;
        String email;
        try {
            if (item == -1) {
                findnearestid.setInt(1, item);
                ResultSet rslt = findnearestid.executeQuery();
                if (rslt.next()) {
                    item = rslt.getInt("guestid");
                } else {
                    return -1;
                }
            }
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                // JOptionPane.showMessageDialog(frame,
                // "Guest Number" + item + " does not exist",
                // "Database Error",
                // JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            name = rslt.getString("name");
            phone = rslt.getString("phone");
            email = rslt.getString("email");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return item;
        }
        JLabel lab1 = new JLabel("Guest Number :");
        JLabel lab2 = new JLabel("Guest Name :");
        JLabel lab3 = new JLabel("Guest Phone Number :");
        JLabel lab4 = new JLabel("Guest Email :");

        JTextField fl1 = new JTextField(String.valueOf(item), 15);
        fl1.setEditable(false);
        JTextField fl2 = new JTextField(name, 15);
        fl2.setEditable(false);
        JTextField fl3 = new JTextField(phone, 15);
        fl3.setEditable(false);
        JTextField fl4 = new JTextField(email, 15);
        fl4.setEditable(false);

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
        return item;
    }

    public void renderall(JPanel panel, Contentpanel cp) {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
        int i = 0;
        GridBagConstraints con = new GridBagConstraints();
        try {
            ResultSet rslt = getallentry.executeQuery();
            while (rslt.next()) {
                int guestid = rslt.getInt("guestid");
                String name = rslt.getString("name");
                String phone = rslt.getString("phone");
                String email = rslt.getString("email");
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
                JLabel lab1 = new JLabel("Guest Number : " + guestid);
                tile.add(lab1, con);
                con.gridx = 1;
                JLabel lab2 = new JLabel("Guest Name : " + name);
                tile.add(lab2, con);
                con.gridx = 2;
                JLabel lab3 = new JLabel("Guest Phone : " + phone);
                tile.add(lab3, con);
                con.gridx = 0;
                con.gridy = 1;
                JLabel lab4 = new JLabel("Guest email : " + email);
                tile.add(lab4, con);
                Hotelmanager self = this;
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ex) {
                        cp.infopanelcurrent = self.getinfo(guestid, cp.panel.infopanel);
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

class Booking implements Hotelmanager {
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
    PreparedStatement findnearestid;

    Booking(JFrame frame) {
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
            findnearestid = Database.dbconn
                    .prepareStatement("SELECT * FROM booking ORDER BY ABS(booking.bookingid - ?) LIMIT 1;");
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

    // add bounds check to prevent double booking
    public void insert() {
        JDialog dialog = new JDialog(frame, "Create New Booking");
        dialog.setSize(500, 250);
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
        con.insets = new Insets(4, 4, 4, 0);
        dialog.add(lab1, con);
        con.gridy = 1;
        dialog.add(lab2, con);
        con.gridy = 2;
        dialog.add(lab3, con);
        con.gridy = 3;
        dialog.add(lab4, con);

        con.insets = new Insets(4, 0, 4, 4);
        con.gridx = 1;
        con.gridy = 0;
        JSpinner spi1 = new JSpinner(new SpinnerDateModel());
        spi1.setEditor(new JSpinner.DateEditor(spi1, "dd-MM-yyyy"));
        dialog.add(spi1, con);

        con.gridy = 1;
        JSpinner spi2 = new JSpinner(new SpinnerDateModel());
        spi2.setEditor(new JSpinner.DateEditor(spi2, "dd-MM-yyyy"));
        dialog.add(spi2, con);

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
                    Date in = (Date) spi1.getValue();
                    Date out = (Date) spi2.getValue();
                    insertentry.setDate(1, new java.sql.Date(in.getTime()));
                    insertentry.setDate(2, new java.sql.Date(out.getTime()));
                    insertentry.setInt(3, (int) cmb1.getSelectedItem());
                    insertentry.setInt(4, (int) cmb2.getSelectedItem());
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
        if (item == -1) {
            return item;
        }
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Booking no " + item + " does not exist",
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
                return rs.getInt("guestid");

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
        if (item == -1) {
            return;
        }
        java.sql.Date checkin;
        java.sql.Date checkout;
        int roomid;
        int guestid;
        try {
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                JOptionPane.showMessageDialog(frame,
                        "Booking Number" + item + " does not exist",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            checkin = rslt.getDate("checkin");
            checkout = rslt.getDate("checkout");
            roomid = rslt.getInt("roomid");
            guestid = rslt.getInt("guestid");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(frame, "Edit Booking Details");
        dialog.setSize(500, 250);
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
        con.insets = new Insets(4, 4, 4, 0);
        dialog.add(lab1, con);
        con.gridy = 1;
        dialog.add(lab2, con);
        con.gridy = 2;
        dialog.add(lab3, con);
        con.gridy = 3;
        dialog.add(lab4, con);

        con.insets = new Insets(4, 0, 4, 4);
        con.gridx = 1;
        con.gridy = 0;
        JSpinner spi1 = new JSpinner(new SpinnerDateModel());
        spi1.setEditor(new JSpinner.DateEditor(spi1, "dd-MM-yyyy"));
        spi1.setValue(checkin);
        dialog.add(spi1, con);

        con.gridy = 1;
        JSpinner spi2 = new JSpinner(new SpinnerDateModel());
        spi2.setEditor(new JSpinner.DateEditor(spi2, "dd-MM-yyyy"));
        spi2.setValue(checkout);
        dialog.add(spi2, con);

        con.gridy = 2;
        JComboBox<Integer> cmb1 = new JComboBox<Integer>(roomids());
        cmb1.setSelectedItem(roomid);
        dialog.add(cmb1, con);
        con.gridy = 3;
        JComboBox<Integer> cmb2 = new JComboBox<Integer>(guestids());
        cmb2.setSelectedItem(guestid);
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
                    Date in = (Date) spi1.getValue();
                    Date out = (Date) spi2.getValue();
                    insertentry.setDate(1, new java.sql.Date(in.getTime()));
                    insertentry.setDate(2, new java.sql.Date(out.getTime()));
                    insertentry.setInt(3, (int) cmb1.getSelectedItem());
                    insertentry.setInt(4, (int) cmb2.getSelectedItem());
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

    public int getinfo(int item, JPanel panel) {
        java.sql.Date checkin;
        java.sql.Date checkout;
        int roomid;
        int guestid;
        int bill;
        try {
            if (item == -1) {
                findnearestid.setInt(1, item);
                ResultSet rslt = findnearestid.executeQuery();
                if (rslt.next()) {
                    item = rslt.getInt("bookingid");
                } else {
                    return -1;
                }
            }
            getentry.setInt(1, item);
            ResultSet rslt = getentry.executeQuery();
            if (!rslt.next()) {
                // JOptionPane.showMessageDialog(frame,
                // "Booking Number" + item + " does not exist",
                // "Database Error",
                // JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            checkin = rslt.getDate("checkin");
            checkout = rslt.getDate("checkout");
            roomid = rslt.getInt("roomid");
            guestid = rslt.getInt("guestid");
            checkexistroom.setInt(1, roomid);
            rslt = checkexistroom.executeQuery();
            rslt.next();
            LocalDate localDate1 = checkin.toLocalDate();
            LocalDate localDate2 = checkout.toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(localDate1, localDate2);
            bill = (int) daysBetween * rslt.getInt("price");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    ex,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return item;
        }
        JLabel lab1 = new JLabel("Booking Number :");
        JLabel lab2 = new JLabel("Checkin Date :");
        JLabel lab3 = new JLabel("Checkout Date :");
        JLabel lab4 = new JLabel("Room Number :");
        JLabel lab5 = new JLabel("Guest Number :");
        JLabel lab6 = new JLabel("Calculated Bill :");

        JTextField fl1 = new JTextField(String.valueOf(item), 15);
        fl1.setEditable(false);
        JTextField fl2 = new JTextField(checkin.toString(), 15);
        fl2.setEditable(false);
        JTextField fl3 = new JTextField(checkout.toString(), 15);
        fl3.setEditable(false);
        JTextField fl4 = new JTextField(String.valueOf(roomid), 15);
        fl4.setEditable(false);
        JTextField fl5 = new JTextField(String.valueOf(guestid), 15);
        fl5.setEditable(false);
        JTextField fl6 = new JTextField(String.valueOf(bill), 15);
        fl6.setEditable(false);

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
        con.gridy = 3;
        panel.add(lab4, con);
        con.gridy = 4;
        panel.add(lab5, con);
        con.gridy = 5;
        panel.add(lab6, con);

        con.gridx = 1;
        con.gridy = 0;
        panel.add(fl1, con);
        con.gridy = 1;
        panel.add(fl2, con);
        con.gridy = 2;
        panel.add(fl3, con);
        con.gridy = 3;
        panel.add(fl4, con);
        con.gridy = 4;
        panel.add(fl5, con);
        con.gridy = 5;
        panel.add(fl6, con);

        con.gridx = 0;
        con.gridy = 6;
        con.fill = GridBagConstraints.BOTH;
        con.gridwidth = 2;
        con.weighty = 1000;
        panel.add(new JPanel(), con);
        return item;
    }

    public void renderall(JPanel panel, Contentpanel cp) {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
        int i = 0;
        GridBagConstraints con = new GridBagConstraints();
        try {
            ResultSet rslt = getallentry.executeQuery();
            while (rslt.next()) {
                int bookingid = rslt.getInt("bookingid");
                Date checkin = rslt.getDate("checkin");
                Date checkout = rslt.getDate("checkout");
                int roomid = rslt.getInt("roomid");
                int guestid = rslt.getInt("guestid");
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
                JLabel lab1 = new JLabel("Booking Number : " + bookingid);
                tile.add(lab1, con);

                con.gridx = 1;
                JLabel lab4 = new JLabel("Booked Room Number : " + roomid);
                tile.add(lab4, con);

                con.gridx = 2;
                JLabel lab5 = new JLabel("Booked Guest Number : " + guestid);
                tile.add(lab5, con);

                con.gridy = 1;
                con.gridx = 1;
                JLabel lab2 = new JLabel("Checkin Date : " + checkin);
                tile.add(lab2, con);

                con.gridx = 2;
                JLabel lab3 = new JLabel("Checkout Date : " + checkout);
                tile.add(lab3, con);

                Hotelmanager self = this;
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ex) {
                        cp.infopanelcurrent = self.getinfo(bookingid, cp.panel.infopanel);
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
        room = new Contentpanel(roommgmt, new Room(frame));
        frame.add(roommgmt.main, "room");
        guestmgmt = new Cardpanel(2, navlist);
        guest = new Contentpanel(guestmgmt, new Guest(frame));
        frame.add(guestmgmt.main, "guest");
        bookingmgmt = new Cardpanel(3, navlist);
        booking = new Contentpanel(bookingmgmt, new Booking(frame));
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
