import java.util.Scanner;

/**
 * Main class that provides the command-line interface for the ticket system
 */
public class TicketSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TicketPool ticketPool = new TicketPool();
        Database database = new Database();

        System.out.println("Welcome to the Real-Time Ticketing System!");

        // Main command loop
        while (true) {
            System.out.println("\nCommands: configure | start | stop | exit");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim();

            switch (command.toLowerCase()) {
                case "configure":
                    // Configure the system
                    ticketPool.configureSystem(scanner, database);
                    break;

                case "start":
                    // Start the system if configured
                    if (ticketPool.isConfigured()) {
                        ticketPool.startTicketHandling();
                    } else {
                        System.out.println("Configuration incomplete. Use 'configure' to set up the system first.");
                    }
                    break;

                case "stop":
                    // Stop the system
                    ticketPool.stopTicketHandling();
                    break;

                case "exit":
                    // Exit the application
                    System.out.println("Exiting system...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid command. Please use 'configure', 'start', 'stop', or 'exit'.");
            }
        }
    }
}