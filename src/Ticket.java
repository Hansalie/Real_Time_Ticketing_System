/**
 * The Ticket class represents a single ticket in the system
 */
public class Ticket {
    private final String eventName;  // Name of the event this ticket is for
    private final int vendorId;      // ID of the vendor who issued this ticket

    /**
     * Constructor for creating a new ticket
     *
     * @param eventName Name of the event
     * @param vendorId ID of the vendor
     */
    public Ticket(String eventName, int vendorId) {
        this.eventName = eventName;
        this.vendorId = vendorId;
    }

    /**
     * Gets the event name for this ticket
     *
     * @return Event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the vendor ID for this ticket
     *
     * @return Vendor ID
     */
    public int getVendorId() {
        return vendorId;
    }
}