import java.time.LocalTime;

public class Main {
    enum Direction {
        NORTH,
        SOUTH
    }

    static Direction curDir = Direction.NORTH;
    static int northCars = 1, southCars = 1, TRIPS = 2, NC = 0, SC = 0, NW = 0, SW = 0;
    static long crossingDelay = 1_000_000_000L, UTurnDelay = 2_000_000_000L;
    static Object gate = new Object();

    public static void bridge (int id, Direction dir) throws InterruptedException{
        long startTime, curTime;
        int trips = TRIPS;
        if (dir == Direction.NORTH) {
            System.out.printf("Car-%d started driving north at %s%n", id, LocalTime.now());
        }
        else {
            System.out.printf("Car-%d started driving south at %s%n", id, LocalTime.now());
        }

        while (trips > 0) {
            synchronized(gate) {
                if (dir == Direction.NORTH) {
                    NW++;
                    System.out.printf("Car-%d is waiting to enter the bridge from the north side at %s%n", id, LocalTime.now());
                    while (dir != curDir) {
                        gate.wait();
                    }
                    NW--;
                    System.out.printf("Car-%d started to cross the bridge from the north side at %s%n", id, LocalTime.now());
                    NC++;
                    startTime = System.nanoTime();
                    while (((curTime = System.nanoTime()) - startTime) < crossingDelay) {
                        gate.wait(100);
                    }
                    NC--;
                    System.out.printf("Car-%d left the bridge at %s%n%n", id, LocalTime.now());
                    dir = Direction.SOUTH;
                    if ((NC == 0 && SW > NW) && SW > 0) {
                        curDir = Direction.SOUTH;
                    }
                    gate.notifyAll();
                    trips--;
                    startTime = System.nanoTime();
                    while (((curTime = System.nanoTime()) - startTime) < UTurnDelay) {
                        gate.wait(100);
                    }
                }
                else if (dir == Direction.SOUTH) {
                    SW++;
                    System.out.printf("Car-%d is waiting to enter the bridge from the south side at %s%n", id, LocalTime.now());
                    while (dir != curDir) {
                        gate.wait();
                    }
                    SW--;
                    System.out.printf("Car-%d started to cross the bridge from the south side at %s%n", id, LocalTime.now());
                    SC++;
                    startTime = System.nanoTime();
                    while (((curTime = System.nanoTime()) - startTime) < crossingDelay) {
                        gate.wait(100);
                    }
                    SC--;
                    System.out.printf("Car-%d left the bridge at %s%n%n", id, LocalTime.now());
                    dir = Direction.NORTH;
                    if ((SC == 0 && NW > SW) && NW > 0) {
                        curDir = Direction.NORTH;
                    }
                    gate.notifyAll();
                    trips--;
                    startTime = System.nanoTime();
                    while (((curTime = System.nanoTime()) - startTime) < UTurnDelay) {
                        gate.wait(100);
                    }
                }
            }
        }
    } // Obama
    
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
