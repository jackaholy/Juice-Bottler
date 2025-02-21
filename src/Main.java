/**
 * Main class to run the Juice Bottling Plant simulation.
 * It creates multiple plants, allows them to process oranges for a fixed time,
 * and then stops them while collecting and printing statistics.
 */
private static final int NUM_PLANTS = 2;

public static void main(String[] args) {
    Plant[] plants = new Plant[NUM_PLANTS];

    for (int i = 0; i < NUM_PLANTS; i++) {
        plants[i] = new Plant(i + 1);
        plants[i].startPlant();
    }

    try {
        Thread.sleep(Plant.PROCESSING_TIME);
    } catch (InterruptedException e) {
        System.err.println("Plant malfunction");
    }

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
    System.out.println("Created " + totalBottles + " bottles, wasted " + totalWasted + " oranges.");
}
