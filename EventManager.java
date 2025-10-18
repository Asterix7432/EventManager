import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventManager {
    private EventDAO eventDAO;
    private Scanner sc;
    
    public EventManager() {
        this.eventDAO = new EventDAO();
        this.sc = new Scanner(System.in);
    }
    
    public static void main(String[] args) {
        System.out.println("Welcome to Event Management System with Database!");
        
        EventManager manager = new EventManager();
        manager.run();
    }
    
    public void run() {
        while (true) {
            displayMenu();
            System.out.print("Enter your choice: ");
            
            try {
                int choice = sc.nextInt();
                sc.nextLine(); // consume newline
                
                switch (choice) {
                    case 1:
                        addEvent();
                        break;
                    case 2:
                        listEvents();
                        break;
                    case 3:
                        editEvent();
                        break;
                    case 4:
                        deleteEvent();
                        break;
                    case 5:
                        searchEvents();
                        break;
                    case 6:
                        manageAttendees();
                        break;
                    case 7:
                        generateReport();
                        break;
                    case 8:
                        System.out.println("Thank you for using Event Management System!");
                        sc.close();
                        DatabaseConnection.getInstance().closeConnection();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // clear invalid input
            }
        }
    }
    
    private void displayMenu() {
        System.out.println("\n=== Event Management System (Database) ===");
        System.out.println("1. Add New Event");
        System.out.println("2. List All Events");
        System.out.println("3. Edit Event");
        System.out.println("4. Delete Event");
        System.out.println("5. Search Events");
        System.out.println("6. Manage Attendees");
        System.out.println("7. Generate Report");
        System.out.println("8. Exit");
    }
    
    private void addEvent() {
        System.out.println("\n--- Add New Event ---");
        
        try {
            System.out.print("Enter event name: ");
            String name = sc.nextLine();
            
            System.out.print("Enter event date (yyyy-MM-dd): ");
            String dateStr = sc.nextLine();
            
            System.out.print("Enter event location: ");
            String location = sc.nextLine();
            
            System.out.print("Enter event description: ");
            String description = sc.nextLine();
            
            System.out.print("Enter event capacity: ");
            int capacity = sc.nextInt();
            sc.nextLine(); // consume newline
            
            if (capacity <= 0) {
                System.out.println("Capacity must be greater than 0. Event not added.");
                return;
            }
            
            Event newEvent = new Event(0, name, dateStr, location, description, capacity);
            
            if (eventDAO.createEvent(newEvent)) {
                System.out.println("Event added successfully with ID: " + newEvent.getId());
            } else {
                System.out.println("Failed to add event. Please try again.");
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Event not added.");
            sc.nextLine(); // clear invalid input
        }
    }
    
    private void listEvents() {
        List<Event> events = eventDAO.getAllEvents();
        
        if (events.isEmpty()) {
            System.out.println("\nNo events found.");
            return;
        }
        
        System.out.println("\n--- All Events ---");
        for (Event event : events) {
            event.display();
        }
    }
    
    private void editEvent() {
        List<Event> events = eventDAO.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("\nNo events to edit.");
            return;
        }
        
        System.out.println("\n--- Edit Event ---");
        System.out.print("Enter event ID to edit: ");
        
        try {
            int eventId = sc.nextInt();
            sc.nextLine(); // consume newline
            
            Event eventToEdit = eventDAO.getEventById(eventId);
            if (eventToEdit == null) {
                System.out.println("Event with ID " + eventId + " not found.");
                return;
            }
            
            System.out.println("Current event details:");
            eventToEdit.display();
            
            System.out.println("\nWhat would you like to edit?");
            System.out.println("1. Name");
            System.out.println("2. Date");
            System.out.println("3. Location");
            System.out.println("4. Description");
            System.out.println("5. Capacity");
            System.out.println("6. Status");
            System.out.print("Enter choice: ");
            
            int editChoice = sc.nextInt();
            sc.nextLine(); // consume newline
            
            switch (editChoice) {
                case 1:
                    System.out.print("Enter new name: ");
                    eventToEdit.setName(sc.nextLine());
                    break;
                case 2:
                    System.out.print("Enter new date (yyyy-MM-dd): ");
                    eventToEdit.setDate(sc.nextLine());
                    break;
                case 3:
                    System.out.print("Enter new location: ");
                    eventToEdit.setLocation(sc.nextLine());
                    break;
                case 4:
                    System.out.print("Enter new description: ");
                    eventToEdit.setDescription(sc.nextLine());
                    break;
                case 5:
                    System.out.print("Enter new capacity: ");
                    int newCapacity = sc.nextInt();
                    sc.nextLine(); // consume newline
                    if (newCapacity > 0) {
                        eventToEdit.setCapacity(newCapacity);
                    } else {
                        System.out.println("Capacity must be greater than 0.");
                    }
                    break;
                case 6:
                    System.out.println("Select new status:");
                    System.out.println("1. PLANNED");
                    System.out.println("2. ONGOING");
                    System.out.println("3. COMPLETED");
                    System.out.println("4. CANCELLED");
                    System.out.print("Enter choice: ");
                    int statusChoice = sc.nextInt();
                    sc.nextLine(); // consume newline
                    
                    switch (statusChoice) {
                        case 1: eventToEdit.setStatus("PLANNED"); break;
                        case 2: eventToEdit.setStatus("ONGOING"); break;
                        case 3: eventToEdit.setStatus("COMPLETED"); break;
                        case 4: eventToEdit.setStatus("CANCELLED"); break;
                        default: System.out.println("Invalid status choice.");
                    }
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            
            if (eventDAO.updateEvent(eventToEdit)) {
                System.out.println("Event updated successfully!");
            } else {
                System.out.println("Failed to update event. Please try again.");
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            sc.nextLine(); // clear invalid input
        }
    }
    
    private void deleteEvent() {
        List<Event> events = eventDAO.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("\nNo events to delete.");
            return;
        }
        
        System.out.println("\n--- Delete Event ---");
        System.out.print("Enter event ID to delete: ");
        
        try {
            int eventId = sc.nextInt();
            sc.nextLine(); // consume newline
            
            Event eventToDelete = eventDAO.getEventById(eventId);
            if (eventToDelete == null) {
                System.out.println("Event with ID " + eventId + " not found.");
                return;
            }
            
            System.out.println("Event to delete:");
            eventToDelete.display();
            System.out.print("Are you sure you want to delete this event? (y/n): ");
            String confirmation = sc.nextLine();
            
            if (confirmation.toLowerCase().equals("y") || confirmation.toLowerCase().equals("yes")) {
                if (eventDAO.deleteEvent(eventId)) {
                    System.out.println("Event deleted successfully!");
                } else {
                    System.out.println("Failed to delete event. Please try again.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            sc.nextLine(); // clear invalid input
        }
    }
    
    private void searchEvents() {
        List<Event> events = eventDAO.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("\nNo events to search.");
            return;
        }
        
        System.out.println("\n--- Search Events ---");
        System.out.println("1. Search by name");
        System.out.println("2. Search by location");
        System.out.println("3. Search by date range");
        System.out.println("4. Search by status");
        System.out.print("Enter choice: ");
        
        try {
            int searchChoice = sc.nextInt();
            sc.nextLine(); // consume newline
            
            switch (searchChoice) {
                case 1:
                    System.out.print("Enter name to search: ");
                    String searchName = sc.nextLine();
                    searchByName(searchName);
                    break;
                case 2:
                    System.out.print("Enter location to search: ");
                    String searchLocation = sc.nextLine();
                    searchByLocation(searchLocation);
                    break;
                case 3:
                    System.out.print("Enter start date (yyyy-MM-dd): ");
                    String startDate = sc.nextLine();
                    System.out.print("Enter end date (yyyy-MM-dd): ");
                    String endDate = sc.nextLine();
                    searchByDateRange(startDate, endDate);
                    break;
                case 4:
                    System.out.println("Select status:");
                    System.out.println("1. PLANNED");
                    System.out.println("2. ONGOING");
                    System.out.println("3. COMPLETED");
                    System.out.println("4. CANCELLED");
                    System.out.print("Enter choice: ");
                    int statusChoice = sc.nextInt();
                    sc.nextLine(); // consume newline
                    
                    String status = "";
                    switch (statusChoice) {
                        case 1: status = "PLANNED"; break;
                        case 2: status = "ONGOING"; break;
                        case 3: status = "COMPLETED"; break;
                        case 4: status = "CANCELLED"; break;
                        default: System.out.println("Invalid status choice."); return;
                    }
                    searchByStatus(status);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            sc.nextLine(); // clear invalid input
        }
    }
    
    private void searchByName(String name) {
        System.out.println("\nSearch results for name: " + name);
        List<Event> events = eventDAO.searchEventsByName(name);
        
        if (events.isEmpty()) {
            System.out.println("No events found with that name.");
        } else {
            for (Event event : events) {
                event.display();
            }
        }
    }
    
    private void searchByLocation(String location) {
        System.out.println("\nSearch results for location: " + location);
        List<Event> events = eventDAO.searchEventsByLocation(location);
        
        if (events.isEmpty()) {
            System.out.println("No events found in that location.");
        } else {
            for (Event event : events) {
                event.display();
            }
        }
    }
    
    private void searchByDateRange(String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            System.out.println("\nSearch results for date range: " + startDateStr + " to " + endDateStr);
            List<Event> events = eventDAO.searchEventsByDateRange(startDate, endDate);
            
            if (events.isEmpty()) {
                System.out.println("No events found in that date range.");
            } else {
                for (Event event : events) {
                    event.display();
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid date format.");
        }
    }
    
    private void searchByStatus(String status) {
        System.out.println("\nSearch results for status: " + status);
        List<Event> events = eventDAO.searchEventsByStatus(status);
        
        if (events.isEmpty()) {
            System.out.println("No events found with that status.");
        } else {
            for (Event event : events) {
                event.display();
            }
        }
    }
    
    private void manageAttendees() {
        List<Event> events = eventDAO.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("\nNo events to manage attendees for.");
            return;
        }
        
        System.out.println("\n--- Manage Attendees ---");
        System.out.print("Enter event ID: ");
        
        try {
            int eventId = sc.nextInt();
            sc.nextLine(); // consume newline
            
            Event event = eventDAO.getEventById(eventId);
            if (event == null) {
                System.out.println("Event with ID " + eventId + " not found.");
                return;
            }
            
            System.out.println("Current event details:");
            event.display();
            
            System.out.println("\n1. Add attendee");
            System.out.println("2. Remove attendee");
            System.out.println("3. View attendees");
            System.out.print("Enter choice: ");
            
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    if (event.isFull()) {
                        System.out.println("Event is full. Cannot add more attendees.");
                    } else {
                        event.addAttendee();
                        if (eventDAO.updateEvent(event)) {
                            System.out.println("Attendee added. Current attendees: " + event.getCurrentAttendees());
                        } else {
                            System.out.println("Failed to update attendees.");
                        }
                    }
                    break;
                case 2:
                    if (event.getCurrentAttendees() > 0) {
                        event.removeAttendee();
                        if (eventDAO.updateEvent(event)) {
                            System.out.println("Attendee removed. Current attendees: " + event.getCurrentAttendees());
                        } else {
                            System.out.println("Failed to update attendees.");
                        }
                    } else {
                        System.out.println("No attendees to remove.");
                    }
                    break;
                case 3:
                    System.out.println("Current attendees: " + event.getCurrentAttendees() + "/" + event.getCapacity());
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            sc.nextLine(); // clear invalid input
        }
    }
    
    private void generateReport() {
        List<Event> events = eventDAO.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("\nNo events to generate report for.");
            return;
        }
        
        EventDAO.EventStatistics stats = eventDAO.getEventStatistics();
        
        System.out.println("\n=== Event Management Report (Database) ===");
        System.out.println("Total Events: " + stats.totalEvents);
        
        System.out.println("\nStatus Summary:");
        System.out.println("Planned: " + stats.plannedEvents);
        System.out.println("Ongoing: " + stats.ongoingEvents);
        System.out.println("Completed: " + stats.completedEvents);
        System.out.println("Cancelled: " + stats.cancelledEvents);
        
        System.out.println("\nCapacity Summary:");
        System.out.println("Total Capacity: " + stats.totalCapacity);
        System.out.println("Total Attendees: " + stats.totalAttendees);
        System.out.println("Overall Occupancy: " + String.format("%.1f%%", stats.getOccupancyRate()));
        
        System.out.println("\nEvent List:");
        for (Event event : events) {
            event.displaySummary();
        }
    }
}
