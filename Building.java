//Building.java
public class Building {
    public static void main(String[] args) {
        Elevatorr elevator = new Elevatorr(100);
        Thread elevatorThread = new Thread(elevator);
        elevatorThread.start();

        try {
            new SimpleHttpServer(elevator).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

