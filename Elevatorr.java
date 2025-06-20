import java.util.*;

public class Elevatorr implements Runnable {
    private int currentFloor = 0;
    private boolean goingUp = true;
    private boolean waitingForDestination = false;
    private final int maxFloor;

    private final TreeSet<Integer> pickupRequests = new TreeSet<>();
    private final TreeSet<Integer> destinationRequests = new TreeSet<>();

    public Elevatorr(int maxFloor) {
        this.maxFloor = maxFloor;
    }

    public synchronized void addPickupRequest(int floor) {
        if (floor >= 0 && floor <= maxFloor) {
            pickupRequests.add(floor);
            notifyAll();
        }
    }

    public synchronized void addDestinationRequest(int floor) {
        if (floor >= 0 && floor <= maxFloor) {
            destinationRequests.add(floor);
            waitingForDestination = false;
            notifyAll();
        }
    }

    public synchronized int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized boolean isWaitingForDestination() {
        return waitingForDestination;
    }

    private synchronized boolean hasRequests() {
        return !pickupRequests.isEmpty() || !destinationRequests.isEmpty();
    }

    private synchronized boolean shouldStopAtCurrentFloor() {
        return pickupRequests.contains(currentFloor) || destinationRequests.contains(currentFloor);
    }

    private synchronized void processStop() {
        boolean stopped = false;
    
        if (pickupRequests.remove(currentFloor)) {
            System.out.println("Pickup at floor: " + currentFloor);
            waitingForDestination = true;
            stopped = true;
        }
    
        if (destinationRequests.remove(currentFloor)) {
            System.out.println("Drop-off at floor: " + currentFloor);
            stopped = true;
        }
    
        if (stopped) {
            try {
                wait(4000); // Pause at the floor for 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    

    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    while (!hasRequests() || waitingForDestination) {
                        wait();
                    }
                }

                synchronized (this) {
                    if (shouldStopAtCurrentFloor()) {
                        processStop();
                    }

                    if (waitingForDestination) {
                        System.out.println("Waiting 10 seconds for destination input...");
                        wait(10000);
                        waitingForDestination = false;
                        continue;
                    }

                    Integer nextFloor = getNextFloor();
                    if (nextFloor != null) {
                        goingUp = nextFloor > currentFloor;

                        while (currentFloor != nextFloor) {
                            currentFloor += goingUp ? 1 : -1;
                            System.out.println("Moving... Floor: " + currentFloor);

                            if (shouldStopAtCurrentFloor()) {
                                processStop();

                                if (waitingForDestination) {
                                    System.out.println("Waiting 10 seconds for destination input...");
                                    wait(10000);
                                    waitingForDestination = false;
                                    break; // Stop further movement until destination is entered
                                }
                            }

                            wait(1000); // 1 second per floor
                        }
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Integer getNextFloor() {
        TreeSet<Integer> allRequests = new TreeSet<>();
        allRequests.addAll(pickupRequests);
        allRequests.addAll(destinationRequests);

        if (goingUp) {
            return allRequests.ceiling(currentFloor + 1) != null
                    ? allRequests.ceiling(currentFloor + 1)
                    : allRequests.floor(currentFloor - 1);
        } else {
            return allRequests.floor(currentFloor - 1) != null
                    ? allRequests.floor(currentFloor - 1)
                    : allRequests.ceiling(currentFloor + 1);
        }
    }
}
