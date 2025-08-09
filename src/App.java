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

}

class Booking {

}

class HotelManager {

}

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }
}
