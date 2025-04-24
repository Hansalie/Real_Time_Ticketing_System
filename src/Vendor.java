import java.util.Random;

/**
 * The Vendor class represents entities that add tickets to the system
 */
public class Vendor implements Runnable {
    private final TicketPool ticketPool;  // Reference to the ticket pool
    private final int vendorId;          // Unique ID for this vendor
    private final String eventName;      // Name of the event this vendor sells tickets for

    /**
     * Constructor for creating a new vendor
     *
     * @param ticketPool Reference to the TicketPool
     * @param vendorId Unique ID for this vendor
     * @param eventName Name of the event
     */
    public Vendor(TicketPool ticketPool, int vendorId, String eventName) {
        this.ticketPool = ticketPool;
        this.vendorId = vendorId;
        this.eventName = eventName;
    }

    /**
     * Run method that executes vendor behavior in a separate thread
     */
    @Override
    public void run() {
        Random random = new Random();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Determine random number of tickets to add (within configured rate)
                int ticketsToAdd = random.nextInt(ticketPool.ticketReleaseRate) + 1;

                // Add tickets to the pool
                ticketPool.addTickets(eventName, vendorId, ticketsToAdd);

                // Sleep for a time period based on the release rate
                Thread.sleep(20000 / ticketPool.ticketReleaseRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}