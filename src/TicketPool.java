import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;

public class TicketPool {
    int totalTickets;
    int ticketReleaseRate;
    int customerRetrievalRate;
    int maxTicketCapacity;
    private boolean configured = false;
    private volatile boolean running = false;
    private int ticketsAdded = 0; // Tracks total tickets added across all vendors
    private int ticketsSold = 0;  // Tracks total tickets sold
    private int currentTickets = 0; // Tracks tickets currently in the pool

    // Thread-safe collection to store available tickets
    private final List<Ticket> ticketPool = Collections.synchronizedList(new ArrayList<>());
    private final ReentrantLock lock = new ReentrantLock();
    private final Semaphore semaphore = new Semaphore(0);

    private static final Logger logger = Logger.getLogger(TicketPool.class.getName());

    // Lists to keep track of vendor and customer threads
    private final List<Thread> vendorThreads = new ArrayList<>();
    private final List<Thread> customerThreads = new ArrayList<>();


    /**
     * Constructor initializes the logging system
     */
    public TicketPool() {
        configureLogger();
    }

    /**
     * Sets up the logging system to record events to both file and console
     */
    private void configureLogger() {
        try {
            // Configure file logging
            FileHandler fileHandler = new FileHandler("ticket_system.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Configure console logging
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to configure logger: " + e.getMessage());
        }
    }

    /**
     * Configures the ticket system with user input or loads existing configuration
     *
     * @param scanner Scanner for user input
     * @param database Database for persistence
     */
    public void configureSystem(Scanner scanner, Database database) {
        System.out.println("Loading existing configuration...");
        if (database.loadConfiguration(this)) {
            System.out.println("Existing configuration loaded successfully:");
            displayCurrentConfiguration();

            System.out.print("Do you want to reconfigure the system? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                configured = true;
                return;
            }
        }

        // Get configuration parameters from user
        System.out.print("Enter total number of tickets: ");
        totalTickets = getValidInput(scanner, 1, 1000);

        System.out.print("Enter ticket release rate: ");
        ticketReleaseRate = getValidInput(scanner, 1, 50);

        System.out.print("Enter customer retrieval rate: ");
        customerRetrievalRate = getValidInput(scanner, 1, 50);

        System.out.print("Enter max ticket capacity: ");
        maxTicketCapacity = getValidInput(scanner, 1, 1000);

        // Save configuration to database
        database.saveConfiguration(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
        System.out.println("System configured successfully.");
        configured = true;
    }

    /**
     * Validates user input within specified range
     *
     * @param scanner Scanner for user input
     * @param min Minimum valid value
     * @param max Maximum valid value
     * @return Valid integer input from user
     */
    private int getValidInput(Scanner scanner, int min, int max) {
        int value;
        while (true) {
            try {
                value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    break;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid input. Enter a value between " + min + " and " + max + ".");
        }
        return value;
    }

    /**
     * Displays the current system configuration
     */
    public void displayCurrentConfiguration() {
        System.out.println("Total Tickets: " + totalTickets);
        System.out.println("Ticket Release Rate (per 10 seconds): " + ticketReleaseRate);
        System.out.println("Customer Retrieval Rate (per 10 seconds): " + customerRetrievalRate);
        System.out.println("Max Ticket Capacity: " + maxTicketCapacity);
    }

    /**
     * Checks if the system is configured
     *
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Starts the ticket handling process by launching vendor and customer threads
     */
    public void startTicketHandling() {
        if (running) {
            System.out.println("System is already running.");
            return;
        }
        running = true;

        // Create and start vendor threads
        for (int i = 1; i <= 3; i++) { // 3 vendors
            Thread vendorThread = new Thread(new Vendor(this, i, "Event" + i));
            vendorThread.start();
            vendorThreads.add(vendorThread);
        }

        // Create and start customer threads
        for (int i = 1; i <= 10; i++) { // 10 customers
            Thread customerThread = new Thread(new Customer(this, i));
            customerThread.start();
            customerThreads.add(customerThread);
        }

        System.out.println("System started. Vendors and customers are now active.");
    }

    /**
     * Stops the ticket handling process by interrupting all threads
     */
    public void stopTicketHandling() {
        if (!running) {
            System.out.println("System is not running.");
            return;
        }
        running = false;

        // Interrupt all vendor and customer threads
        vendorThreads.forEach(Thread::interrupt);
        customerThreads.forEach(Thread::interrupt);

        System.out.println("System stopped. All operations halted.");
    }

    /**
     * Adds tickets to the pool from a vendor
     *
     * @param eventName Name of the event
     * @param vendorId ID of the vendor
     * @param ticketsToAdd Number of tickets to add
     */
    public void addTickets(String eventName, int vendorId, int ticketsToAdd) {
        lock.lock();
        try {
            // Check if we've reached the total ticket limit
            if (ticketsAdded >= totalTickets) {
                logger.info("Vendor " + vendorId + " attempted to add tickets, but the total ticket limit has been reached.");
                return;
            }

            // Calculate remaining capacity and adjust tickets to add accordingly
            int remainingCapacity = totalTickets - ticketsAdded;
            ticketsToAdd = Math.min(ticketsToAdd, remainingCapacity);

            if (ticketsToAdd > 0) {
                // Add tickets to the pool
                for (int i = 0; i < ticketsToAdd; i++) {
                    Ticket ticket = new Ticket(eventName, vendorId);
                    ticketPool.add(ticket);
                    semaphore.release(); // Signal availability of a new ticket
                }

                ticketsAdded += ticketsToAdd;
                currentTickets += ticketsToAdd; // Update the current ticket count
                logger.info("Vendor " + vendorId + " added " + ticketsToAdd + " ticket(s) for " + eventName);
                logCurrentTickets(); // Log the current tickets after adding
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Processes ticket purchases by customers
     *
     * @param customerId ID of the customer
     */
    public void purchaseTickets(int customerId) {
        Random random = new Random();
        int ticketsToBuy = random.nextInt(customerRetrievalRate) + 1;
        lock.lock();
        try {
            // Check if tickets are available
            if (ticketPool.isEmpty()) {
                logger.warning("Customer " + customerId + " attempted to purchase tickets, but the pool is empty.");
                return;
            }

            // Adjust tickets to buy based on availability
            ticketsToBuy = Math.min(ticketsToBuy, ticketPool.size());

            // Process purchase
            for (int i = 0; i < ticketsToBuy; i++) {
                Ticket ticket = ticketPool.remove(0);
                ticketsSold++;
            }

            currentTickets -= ticketsToBuy; // Decrease the current ticket count
            logger.info("Customer " + customerId + " purchased " + ticketsToBuy + " ticket(s).");
            logCurrentTickets(); // Log the current tickets after purchase

            // Check if all tickets are sold
            if (ticketsSold >= totalTickets) {
                logger.info("All tickets have been sold.");
                System.out.println("Tickets are sold out. Stopping the system...");
                stopTicketHandling();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Logs the current number of tickets in the pool
     */
    private void logCurrentTickets() {
        logger.info("Current tickets in pool: " + currentTickets);
    }
}