public class Worker implements Runnable {
    private final BlockingMailbox<Orange> mailbox;
    private final int workerId;
    private final Plant plant;
    private volatile boolean running = true;

    public Worker(BlockingMailbox<Orange> mailbox, int workerId, Plant plant) {
        this.mailbox = mailbox;
        this.workerId = workerId;
        this.plant = plant;
    }

    public void run() {
        while (running) {
            Orange o = mailbox.get();
            if (o == null) {
                System.out.println("Worker " + workerId + " stopping.");
                break;
            }

            // Check and see if the orange is bottled yet.
            // If not, do work on it.
            while (o.getState() != Orange.State.Bottled) {
                System.out.println("State of Plant #" + plant.getPlantId() + ": " + o.getState());
                o.runProcess();
            }

            plant.incrementProcessedOranges();
            System.out.println("Worker " + workerId + " has bottled an orange.");
        }
    }

    public void stopWorker() {
        running = false;
        mailbox.put(null);
    }
}