import java.util.ArrayList;
import java.sql.Date;
enum ROOM_TYPE {
    SINGLE,
    DOUBLE,
    DELUXE,
}

class Room {
    final int roomnumber;
    ROOM_TYPE roomtype;
    int pricepernight;
    boolean availabilitystatus;

    // directly modify room data
    void changeroomprice(int pricepernight) {
        this.pricepernight = pricepernight;
    }

    void changeroomtype(ROOM_TYPE roomtype) {
        this.roomtype = roomtype;
    }

    void changeroomavailability(Boolean availabilitystatus) {
        this.availabilitystatus = availabilitystatus;
    }

    // adjust room data
    void increaseroomprice(int price) {
        this.pricepernight = this.pricepernight + price;
    }

    void decreaseroomprice(int price) {
        if (price > this.pricepernight) {
            this.pricepernight = 0;
            System.err.println("Room price cannot be decreased beyond 0");
            return;
        } else if (price == this.pricepernight) {
            System.err.println("The room is now available for free");
        }
        this.pricepernight = this.pricepernight - price;
    }

    void downgraderoom() {
        if (this.roomtype == ROOM_TYPE.DELUXE) {
            this.roomtype = ROOM_TYPE.DOUBLE;
            System.out.println("Room has been downgraded to double room");
        } else if (this.roomtype == ROOM_TYPE.DOUBLE) {
            this.roomtype = ROOM_TYPE.SINGLE;
            System.out.println("Room has been downgraded to Single room");
        } else {
            System.out.println("Single is the lowest tier of room cannot be downgraded further");
        }
    }

    void upgraderoom() {
        if (this.roomtype == ROOM_TYPE.SINGLE) {
            this.roomtype = ROOM_TYPE.DOUBLE;
            System.out.println("Room has been upgraded to Double room");
        } else if (this.roomtype == ROOM_TYPE.DOUBLE) {
            this.roomtype = ROOM_TYPE.DELUXE;
            System.out.println("Room has been upgraded to Deluxe room");
        } else {
            System.out.println("Deluxe is the Highest tier of room cannot be upgraded further");
        }
    }

    void toggleavailability() {
        if (this.availabilitystatus == true) {
            this.availabilitystatus = false;
        } else {
            this.availabilitystatus = true;
        }
    }

    // Room constructors
    Room(int roomnumber) {
        this.roomnumber = roomnumber;
        availabilitystatus = true;
    }

    Room(int roomnumber, int pricepernight) {
        this.roomnumber = roomnumber;
        this.pricepernight = pricepernight;
        availabilitystatus = true;
    }

    Room(int roomnumber, ROOM_TYPE roomtype, int pricepernight) {
        this.roomnumber = roomnumber;
        this.pricepernight = pricepernight;
        this.roomtype = roomtype;
        availabilitystatus = true;
    }
}

class Guest {
    String guestname;
    int id;
    String phonenumber;
    String email;
    ArrayList<Booking> booking;
    private int finduniqueID(Guest[] guestlist) {
        boolean found = true;
        int id = 0;
        int i = 0;
        while (i < guestlist.length) {
            if (id == guestlist[i].id) {
                found = false;
                id++;
            }
            i++;
            if (i == guestlist.length && !found) {
                found = true;
                i = 0;
            }
        }
        return id;
    }

    Guest(String guestname, Guest[] guestlist) {
        this.guestname = guestname;
        this.id = finduniqueID(guestlist);
        booking = new ArrayList<Booking>(10);
    }
}

class Booking {
    int bookingid;
    Guest guest;
    Room room;
    Date checkin;
    Date Checkout;
    //The checking date and checkout date can be converted to long using the Date.valueof("yyyy-mm-dd")
    Booking(Guest guest,Room room,long checkindate,long checkoutdate){
        this.guest = guest;
        this.room = room;
        this.checkin = new Date(checkindate);
        this.Checkout = new Date(checkoutdate);
    }

}

class HotelManager {

}

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }
}
