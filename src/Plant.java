import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Plant implements Runnable {
    public static final long PROCESSING_TIME = 5 * 1000;
    public final int ORANGES_PER_BOTTLE = 3;

    private static final int NUM_PLANTS = 2;
    private static final int NUM_WORKERS = 2;

    private final Thread thread;
    private final ExecutorService workers;
    private final BlockingMailbox<Orange> mailbox;
    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;

    private final int plantId;

    public static void main(String[] args) {
        // Startup the plants
        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i + 1, NUM_WORKERS);
            plants[i].startPlant();
            System.out.println("Plant " + (i + 1) + " started");
        }

        // Give the plants time to do work
        delay();

        // Stop the plant, and wait for it to shut down
        for (Plant p : plants) {
            p.stopPlant();
        }

        for (Plant p : plants) {
            p.waitToStop();
        }

        int totalProvided = 0;
        int totalProcessed = 0;
        int totalBottles = 0;
        int totalWasted = 0;
        for (Plant p : plants) {
            totalProvided += p.getProvidedOranges();
            totalProcessed += p.getProcessedOranges();
            totalBottles += p.getBottles();
            totalWasted += p.getWaste();
        }
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles + ", wasted " + totalWasted + " oranges");

    }

    private static void delay() {
        long sleepTime = Math.max(1, Plant.PROCESSING_TIME);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println("Plant malfunction");
        }
    }

    Plant(int plantId, int numWorkers) {
        this.plantId = plantId;
        orangesProvided = 0;
        orangesProcessed = 0;
        mailbox = new BlockingMailbox<>();
        workers = Executors.newFixedThreadPool(numWorkers);
        thread = new Thread(this, "Plant " + plantId);
        // This code came from https://openai.com/
        for (int i = 0; i < numWorkers; i++) {
            int workerId = i + 1;
            workers.submit(() -> workerProcess(workerId));
            System.out.println("Worker " + workerId + " started at Plant " + plantId);
        }
    }

    public void startPlant() {
        timeToWork = true;
        thread.start();
    }

    public void stopPlant() {
        timeToWork = false;
        workers.shutdown();

        for (int i = 0; i < NUM_WORKERS; i++) {
            mailbox.put(null);
        }
    }

    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    public void run() {
        System.out.print(Thread.currentThread().getName() + " Processing oranges");
        while (timeToWork) {
            mailbox.put(new Orange());
            orangesProvided++;
        }
        System.out.println("\n" + Thread.currentThread().getName() + " Done");
    }

    private void workerProcess(int workerId) {
        while (!Thread.currentThread().isInterrupted()) {
            // Check and see if the status of the orange
            Orange o = mailbox.get();
            if (o == null) {
                System.out.println("Worker " + workerId + " at plant " + plantId + " is stopping.");
                break;
            }

            System.out.println("Worker " + workerId + " at Plant " + plantId + " is processing an orange.");

            while (o.getState() != Orange.State.Bottled) {
                System.out.println("Worker " + workerId + " at Plant " + plantId + " is performing: " + o.getState());
                o.runProcess();
            }

            synchronized (this) {
                orangesProcessed++;
            }

            System.out.println("Worker " + workerId + " at Plant " + plantId + " has bottled an orange.");
        }
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        return orangesProcessed;
    }

    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    public int getWaste() {
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }
}