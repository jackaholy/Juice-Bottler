import java.util.ArrayList;
import java.util.List;

/**
 * Plant class processes oranges using worker threads.
 * Each plant has its own thread and distributes oranges to workers to process.
 */
public class Plant implements Runnable {
    public static final long PROCESSING_TIME = 5 * 1000; // How long the process will run.

    private static final int NUM_WORKERS = 2; // The number of workers we want.
    private final Thread thread; // Plant threads.
    private final List<Worker> workers = new ArrayList<>(); // An ArrayList to hold the workers.
    private final List<Thread> workerThreads = new ArrayList<>(); // An ArrayList to hold the threads of the workers.
    private final List<BlockingMailbox<Orange>> mailboxes = new ArrayList<>();
    // An ArrayList to hold the mailboxes.

    private volatile int orangesProvided;
    private volatile int orangesProcessed;
    private volatile boolean timeToWork; // Boolean flag to know when it's working time.
    private final int plantId; // Unique plant ID to keep track of plants.

    /**
     * Plant constructor which creates a new worker.
     *
     * @param plantId A unique number given to each created plant.
     */
    public Plant(int plantId) {
        this.plantId = plantId;
        thread = new Thread(this, "Plant[" + plantId + "]");
        // Creates new workers and mailboxes. I had some help from https://openai.com/.
        for (int i = 0; i < NUM_WORKERS; i++) {
            BlockingMailbox<Orange> mailbox = new BlockingMailbox<>();
            Worker worker = new Worker(mailbox, i + 1, this);
            Thread workerThread = new Thread(worker);
            mailboxes.add(mailbox);
            workers.add(worker);
            workerThreads.add(workerThread);
            workerThread.start();
        }
    }

    /**
     * Starts the plant thread.
     */
    public void startPlant() {
        timeToWork = true;
        thread.start();
    }

    /**
     * Stops the plant threads.
     */
    public void stopPlant() {
        timeToWork = false;

        // Tells workers to stop.
        for (Worker worker : workers) {
            worker.stopWorker();
        }

        // Ensure all workers have fully stopped.
        for (Thread workerThread : workerThreads) {
            workerThread.interrupt();
        }
    }

    /**
     * Waits for the plant thread to stop.
     */
    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    /**
     * Main processing loop. Assigns oranges to workers until processing time is done.
     */
    public void run() {
        long endTime = System.currentTimeMillis() + PROCESSING_TIME;
        int workerIndex = 0;
        // I got this while loop with help from https://openai.com/.
        while (timeToWork && System.currentTimeMillis() < endTime) {
            Orange orange = new Orange();
            mailboxes.get(workerIndex).put(orange);
            incrementProvidedOranges();
            // Give each worker a fair share of oranges.
            workerIndex = (workerIndex + 1) % NUM_WORKERS;
        }
    }

    /**
     * Increment the number of oranges provided in a thread safe way.
     */
    public synchronized void incrementProvidedOranges() {
        orangesProvided++;
    }

    /**
     * Increment the number of oranges processed in a thread safe way.
     */
    public synchronized void incrementProcessedOranges() {
        orangesProcessed++;
    }

    /**
     * Get the number of oranges provided.
     *
     * @return the number of oranges provided.
     */
    public synchronized int getProvidedOranges() {
        return orangesProvided;
    }

    /**
     * Get the number of oranges processed.
     *
     * @return the number of oranges processed.
     */
    public synchronized int getProcessedOranges() {
        return orangesProcessed;
    }

    /**
     * Get the number of bottles of oranges produced.
     * Since each bottle requires three oranges, divide by 3.
     *
     * @return the number of bottles produced.
     */
    public int getBottles() {
        return orangesProcessed / 3;
    }

    /**
     * Get the number of oranges wasted.
     *
     * @return the number of wasted oranges.
     */
    public int getWaste() {
        return orangesProcessed % 3;
    }

    /**
     * The unique ID of a specific plant.
     *
     * @return the plant's ID.
     */
    public int getPlantId() {
        return plantId;
    }
}