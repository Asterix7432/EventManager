import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Event {
    private int id;
    private String name;
    private LocalDate date;
    private String location;
    private String description;
    private int capacity;
    private String status; // PLANNED, ONGOING, COMPLETED, CANCELLED
    private int currentAttendees;
    
    public Event(int id, String name, String dateStr, String location, String description, int capacity) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.currentAttendees = 0;
        this.status = "PLANNED";
        
        // Parse date with error handling
        try {
            this.date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date as default.");
            this.date = LocalDate.now();
        }
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    public int getCurrentAttendees() { return currentAttendees; }
    
    // Setters for editing
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDate(String dateStr) {
        try {
            this.date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Date not updated.");
        }
    }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setStatus(String status) { this.status = status; }
    public void setCurrentAttendees(int currentAttendees) { this.currentAttendees = currentAttendees; }
    
    // Business logic methods
    public boolean isFull() {
        return currentAttendees >= capacity;
    }
    
    public int getAvailableSpots() {
        return capacity - currentAttendees;
    }
    
    public void addAttendee() {
        if (!isFull()) {
            currentAttendees++;
        }
    }
    
    public void removeAttendee() {
        if (currentAttendees > 0) {
            currentAttendees--;
        }
    }
    
    public void display() {
        System.out.println("==========================================");
        System.out.println("Event ID: " + id);
        System.out.println("Event Name: " + name);
        System.out.println("Date: " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        System.out.println("Location: " + location);
        System.out.println("Description: " + description);
        System.out.println("Capacity: " + capacity);
        System.out.println("Current Attendees: " + currentAttendees);
        System.out.println("Available Spots: " + getAvailableSpots());
        System.out.println("Status: " + status);
        System.out.println("==========================================");
    }
    
    public void displaySummary() {
        System.out.println("ID: " + id + " | " + name + " | " + 
                         date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + 
                         " | " + location + " | " + status);
    }
}
