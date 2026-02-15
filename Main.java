import java.time.LocalTime;

public class Main {
    enum Direction {
        NORTH,
        SOUTH
    }

    static Direction curDir = Direction.NORTH;
    static int northCars = 4, southCars = 4, TRIPS = 4, 
    NC = 0, SC = 0, NW = 0, SW = 0, 
    crossingDelay = 1000, UTurnDelay = 2000;
    static Object gate = new Object();

    public static void bridge (int id, Direction dir) throws InterruptedException {
        int trips = TRIPS;
        if (dir == Direction.NORTH) {
            System.out.printf("Car:%d started driving north at %s%n", id, LocalTime.now());
        }
        else {
            System.out.printf("Car:%d started driving south at %s%n", id, LocalTime.now());
        }

        while (trips > 0) {
            if (dir == Direction.NORTH) {
                synchronized(gate) {
                    NW++;
                    System.out.printf("Car:%d is waiting to enter the bridge from the south side at %s%n", id, LocalTime.now());
                    while (dir != curDir) {
                        gate.wait();
                    }
                    NW--;
                    System.out.printf("Car:%d started to cross the bridge from the south side at %s%n", id, LocalTime.now());
                    NC++;
                }
                Thread.sleep(crossingDelay);

                synchronized(gate) {
                    NC--;
                    System.out.printf("Car:%d left the bridge with %d trips left at %s%n", id, trips - 1, LocalTime.now());
                    dir = Direction.SOUTH;
                    if (NC == 0) {
                        curDir = Direction.SOUTH;
                        System.out.printf("%n");
                    }
                    gate.notifyAll();
                    trips--;
                }
                Thread.sleep(UTurnDelay);
            }
            else if (dir == Direction.SOUTH) {
                synchronized(gate) {
                    SW++;
                    System.out.printf("Car:%d is waiting to enter the bridge from the north side at %s%n", id, LocalTime.now());
                    while (dir != curDir) {
                        gate.wait();
                    }
                    SW--;
                    System.out.printf("Car:%d started to cross the bridge from the north side at %s%n", id, LocalTime.now());
                    SC++;
                }
                Thread.sleep(crossingDelay);
                
                synchronized(gate) {
                    SC--;
                    System.out.printf("Car:%d left the bridge with %d trips left at %s%n", id, trips - 1, LocalTime.now());
                    dir = Direction.NORTH;
                    if (SC == 0) {
                        curDir = Direction.NORTH;
                        System.out.printf("%n");
                    }
                    gate.notifyAll();
                    trips--;
                }
                Thread.sleep(UTurnDelay);
            }
        }
    }
    
    public static void main (String[] args) {
        if (args.length == 3) {
            northCars = Integer.parseInt(args[0]);
            southCars = Integer.parseInt(args[1]);
            TRIPS = Integer.parseInt(args[2]);
        }
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
                    Thread.currentThread().interrupt();
                }
            }).start();

        }
    }
}
