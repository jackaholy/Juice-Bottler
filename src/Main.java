/**
 * Main class to run the Juice Bottling Plant simulation.
 * It creates multiple plants, allows them to process oranges for a fixed time,
 * and then stops them while collecting and printing statistics.
 */

// Number of plants to be processing oranges.
private static final int NUM_PLANTS = 2;

/**
 * Main method which runs the juice simulation.
 */
public static void main(String[] args) {
    // Array where all the plants are stored.
    Plant[] plants = new Plant[NUM_PLANTS];

    // For the number of plants we're creating, start each of them.
    for (int i = 0; i < NUM_PLANTS; i++) {
        plants[i] = new Plant(i + 1);
        plants[i].startPlant();
    }

    // Let each plant run for some time.
    try {
        Thread.sleep(Plant.PROCESSING_TIME);
    } catch (InterruptedException e) {
        System.err.println("Plant malfunction");
    }

    // Stop every plant
    for (Plant p : plants) {
        p.stopPlant();
    }

    for (Plant p : plants) {
        p.waitToStop();
    }

    // Keep track of all the plant statistics.
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

    // Print juicing totals and wasted plant totals.
    System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
    System.out.println("Created " + totalBottles + " bottles, wasted " + totalWasted + " oranges.");
}
