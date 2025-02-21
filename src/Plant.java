import java.util.ArrayList;
import java.util.List;

public class Plant implements Runnable {
    public static final long PROCESSING_TIME = 1 * 1000;

    private static final int NUM_WORKERS = 2;
    private final Thread thread;
    private final List<Worker> workers = new ArrayList<>();
    private final List<Thread> workerThreads = new ArrayList<>();
    private final List<BlockingMailbox<Orange>> mailboxes = new ArrayList<>();;

    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;
    private final int plantId;

    public Plant(int plantId) {
        this.plantId = plantId;
        thread = new Thread(this, "Plant[" + plantId + "]");

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

    public void startPlant() {
        timeToWork = true;
        thread.start();
    }

    public void stopPlant() {
        timeToWork = false;

        // Send stop signals to workers
        for (Worker worker : workers) {
            worker.stopWorker();
        }

        // Assigns all mailboxes as null.
        for (BlockingMailbox<Orange> mailbox : mailboxes) {
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
        long endTime = System.currentTimeMillis() + PROCESSING_TIME;
        int workerIndex = 0;

        while (timeToWork && System.currentTimeMillis() < endTime) {
            Orange orange = new Orange();
            mailboxes.get(workerIndex).put(orange);
            incrementProvidedOranges();
            workerIndex = (workerIndex + 1) % NUM_WORKERS;
        }
    }

    public synchronized void incrementProvidedOranges() {
        orangesProvided++;
    }

    public synchronized void incrementProcessedOranges() {
        orangesProcessed++;
    }

    public synchronized int getProvidedOranges() {
        return orangesProvided;
    }

    public synchronized int getProcessedOranges() {
        return orangesProcessed;
    }

    public int getBottles() {
        return orangesProcessed / 3;
    }

    public int getWaste() {
        return orangesProcessed % 3;
    }

    public int getPlantId() {
        return plantId;
    }
}