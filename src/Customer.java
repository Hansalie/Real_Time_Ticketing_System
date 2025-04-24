/**
 * The Customer class represents entities that purchase tickets from the system
 */
public class Customer implements Runnable {
    private final TicketPool ticketPool;  // Reference to the ticket pool
    private final int customerId;        // Unique ID for this customer

    /**
     * Constructor for creating a new customer
     *
     * @param ticketPool Reference to the TicketPool
     * @param customerId Unique ID for this customer
     */
    public Customer(TicketPool ticketPool, int customerId) {
        this.ticketPool = ticketPool;
        this.customerId = customerId;
    }

    /**
     * Run method that executes customer behavior in a separate thread
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Attempt to purchase tickets
                ticketPool.purchaseTickets(customerId);

                // Sleep for a time period based on the retrieval rate
                Thread.sleep(20000 / ticketPool.customerRetrievalRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}