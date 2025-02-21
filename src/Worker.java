/**
 * The workers are responsible for processing oranges through different states of an orange.
 * Each worker has its own thread and will continue processing oranges for a specified amount of time or until stopped.
 */
public class Worker implements Runnable {
    // Each mailbox 'holds' an orange to deal with.
    private final BlockingMailbox<Orange> mailbox;
    private final int workerId;
    private final Plant plant;
    private volatile boolean running = true;

    /**
     * Worker constructor to assign a worker to a mailbox, plant, and a unique ID.
     *
     * @param mailbox  The mailbox which the worker will receive oranges from.
     * @param workerId The unique workerId which the worker will have.
     * @param plant    The plant the worker works at.
     */
    public Worker(BlockingMailbox<Orange> mailbox, int workerId, Plant plant) {
        this.mailbox = mailbox;
        this.workerId = workerId;
        this.plant = plant;
    }

    /**
     * Runs the worker thread, processing oranges until stopped.
     */
    public void run() {
        while (running) {
            // Check to see if there is still an orange present. If no orange is present, stop the worker.
            Orange o = mailbox.get();
            if (o == null) {
                System.out.println("Worker " + workerId + " stopping.");
                stopWorker();
            } else {
                // Check and see if the orange is bottled yet.
                // If not, do work on it.
                while (o.getState() != Orange.State.Bottled) {
                    System.out.println("State of Plant #" + plant.getPlantId() + ": " + o.getState());
                    o.runProcess();
                }
            }

            plant.incrementProcessedOranges();
            System.out.println("Worker " + workerId + " has bottled an orange.");
        }
    }

    /**
     * Stops the worker by setting a 'null' orange in the mailbox to begin stopping the process.
     */
    public void stopWorker() {
        running = false;
        mailbox.put(null);
    }
}