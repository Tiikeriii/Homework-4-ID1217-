import java.time.LocalTime;
import java.util.concurrent.*;

// enum for directions
public class Main {
    enum Direction {
        NORTH,
        SOUTH
    }

    // variables used for logic, synchronisation, and initialization
    static Direction curDir = Direction.NORTH;
    static int northCars = 4, southCars = 4, TRIPS = 4, THRESHOLD = 4, 
    CarsOnBridge = 0, totalCars = 0, NW = 0, SW = 0,    // NW and SW is for northbound cars waiting and southbound cars waiting
    minCrossingDelay = 2000, minUTurnDelay = 4000, maxCrossingDelay = 4000, maxUTurnDelay = 8000;
    static boolean SWITCH = false;
    static Object gate = new Object();

    // function for simulating a "One lane bridge"
    public static void bridge (int id, Direction dir) throws InterruptedException {
        int trips = TRIPS;
        // prints out which direction each car starts driving and the timestamp
        if (dir == Direction.NORTH) {
            System.out.printf("Car:%d started driving north at %s%n", id, LocalTime.now());
        }
        else {
            System.out.printf("Car:%d started driving south at %s%n", id, LocalTime.now());
        }

        // while loop that iterates until the car is done driving
        while (trips > 0) {
            // northbound branch
            if (dir == Direction.NORTH) {
                // acquire the lock  and initiate the waiting procedure
                synchronized(gate) {
                    NW++;
                    System.out.printf("Car:%d is waiting to enter the bridge from the south side at %s%n", id, LocalTime.now());
                    // sleep while the active direction is not your current direction or when a threshold has been met
                    while (dir != curDir || SWITCH) {
                        gate.wait();
                    }
                    // enter the bridge
                    NW--;
                    System.out.printf("Car:%d started to cross the bridge from the south side at %s%n", id, LocalTime.now());
                    CarsOnBridge++;
                    totalCars++;
                    // if statement for fairness
                    if (totalCars == THRESHOLD) {
                        SWITCH = true;
                    }
                }
                // delay used for simulating the crossing time
                Thread.sleep(ThreadLocalRandom.current().nextInt(minCrossingDelay, maxCrossingDelay));

                // exit the bridge and switch active direction if the bridge is empty and (the threshold of cars passed has been met or there are more cars waiting in the opposite direction)
                synchronized(gate) {
                    CarsOnBridge--;
                    System.out.printf("Car:%d left the bridge with %d trips left at %s%n", id, trips - 1, LocalTime.now());
                    dir = Direction.SOUTH;
                    if (CarsOnBridge == 0 && (SWITCH || SW >= NW)) {
                        curDir = Direction.SOUTH;
                        SWITCH = false;
                        totalCars = 0;
                        System.out.printf("%n");
                    }
                    // wake all sleeping thredas so they recheck their conditions
                    gate.notifyAll();
                    trips--;
                }
                // delay to simulate a U-turn
                Thread.sleep(ThreadLocalRandom.current().nextInt(minUTurnDelay, maxUTurnDelay));
            }
            // southbound branch
            else if (dir == Direction.SOUTH) {
                // acquire the lock  and initiate the waiting procedure
                synchronized(gate) {
                    SW++;
                    System.out.printf("Car:%d is waiting to enter the bridge from the north side at %s%n", id, LocalTime.now());
                    // sleep while the active direction is not your current direction or when a threshold has been met
                    while (dir != curDir || SWITCH) {
                        gate.wait();
                    }
                    // enter the bridge
                    SW--;
                    System.out.printf("Car:%d started to cross the bridge from the north side at %s%n", id, LocalTime.now());
                    CarsOnBridge++;
                    totalCars++;
                    // if statement for fairness
                    if (totalCars == THRESHOLD) {
                        SWITCH = true;
                    }
                }
                // delay used for simulating the crossing time
                Thread.sleep(ThreadLocalRandom.current().nextInt(minCrossingDelay, maxCrossingDelay));
                
                // exit the bridge and switch active direction if the bridge is empty and (the threshold of cars passed has been met or there are more cars waiting in the opposite direction)
                synchronized(gate) {
                    CarsOnBridge--;
                    System.out.printf("Car:%d left the bridge with %d trips left at %s%n", id, trips - 1, LocalTime.now());
                    dir = Direction.NORTH;
                    if (CarsOnBridge == 0 && (SWITCH || NW >= SW)) {
                        curDir = Direction.NORTH;
                        SWITCH = false;
                        totalCars = 0;
                        System.out.printf("%n");
                    }
                    // wake all sleeping thredas so they recheck their conditions
                    gate.notifyAll();
                    trips--;
                }
                // delay to simulate a U-turn
                Thread.sleep(ThreadLocalRandom.current().nextInt(minUTurnDelay, maxUTurnDelay));
            }
        }
    }
    
    public static void main (String[] args) {
        // set amount of cars and trips if there are user argument, else, default values are defined
        if (args.length == 3) {
            northCars = Integer.parseInt(args[0]);
            southCars = Integer.parseInt(args[1]);
            TRIPS = Integer.parseInt(args[2]);
        }
        // create threads and aassign them a direction and unique id.
        for (int i = 0; i < northCars + southCars; i++) {
            int id = i;
            Direction dir;

            if (i < northCars) 
                dir = Direction.NORTH;
            else 
                dir = Direction.SOUTH;

            new Thread(() -> {
                try {
                    bridge(id, dir);
                } catch (InterruptedException e) {
                    return;
                }
            }).start();

        }
    }
}
