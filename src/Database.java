import java.sql.*;

/**
 * The Database class handles persistence of system configuration
 */
public class Database {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ticket_syss";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    /**
     * Constructor initializes the database connection and creates tables if needed
     */
    public Database() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            createTables(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates necessary database tables if they don't exist
     *
     * @param conn Database connection
     * @throws SQLException if a database error occurs
     */
    private void createTables(Connection conn) throws SQLException {
        String configTable = "CREATE TABLE IF NOT EXISTS system_config (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "total_tickets INT NOT NULL, " +
                "release_rate INT NOT NULL, " +
                "retrieval_rate INT NOT NULL, " +
                "max_capacity INT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(configTable);
        }
    }

    /**
     * Saves system configuration to the database
     *
     * @param totalTickets Total tickets in the system
     * @param releaseRate Ticket release rate
     * @param retrievalRate Customer retrieval rate
     * @param maxCapacity Maximum ticket capacity
     */
    public void saveConfiguration(int totalTickets, int releaseRate, int retrievalRate, int maxCapacity) {
        String deleteOldConfig = "DELETE FROM system_config";
        String insertConfig = "INSERT INTO system_config (total_tickets, release_rate, retrieval_rate, max_capacity) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteOldConfig);
             PreparedStatement insertStmt = conn.prepareStatement(insertConfig)) {

            deleteStmt.executeUpdate(); // Clear any previous configuration
            insertStmt.setInt(1, totalTickets);
            insertStmt.setInt(2, releaseRate);
            insertStmt.setInt(3, retrievalRate);
            insertStmt.setInt(4, maxCapacity);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads system configuration from the database
     *
     * @param ticketPool TicketPool to populate with configuration
     * @return true if configuration was loaded, false otherwise
     */
    public boolean loadConfiguration(TicketPool ticketPool) {
        String query = "SELECT total_tickets, release_rate, retrieval_rate, max_capacity FROM system_config";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Populate TicketPool configuration fields
                ticketPool.totalTickets = rs.getInt("total_tickets");
                ticketPool.ticketReleaseRate = rs.getInt("release_rate");
                ticketPool.customerRetrievalRate = rs.getInt("retrieval_rate");
                ticketPool.maxTicketCapacity = rs.getInt("max_capacity");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // No configuration found
    }
}