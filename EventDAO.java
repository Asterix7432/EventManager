import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private DatabaseConnection dbConnection;
    
    public EventDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    // Create a new event
    public boolean createEvent(Event event) {
        String sql = "INSERT INTO events (name, event_date, location, description, capacity, current_attendees, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, event.getName());
            pstmt.setDate(2, Date.valueOf(event.getDate()));
            pstmt.setString(3, event.getLocation());
            pstmt.setString(4, event.getDescription());
            pstmt.setInt(5, event.getCapacity());
            pstmt.setInt(6, event.getCurrentAttendees());
            pstmt.setString(7, event.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        event.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating event: " + e.getMessage());
        }
        return false;
    }
    
    // Read all events
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_date ASC";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Event event = new Event(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDate("event_date").toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    rs.getString("location"),
                    rs.getString("description"),
                    rs.getInt("capacity")
                );
                event.setCurrentAttendees(rs.getInt("current_attendees"));
                event.setStatus(rs.getString("status"));
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving events: " + e.getMessage());
        }
        return events;
    }
    
    // Read event by ID
    public Event getEventById(int id) {
        String sql = "SELECT * FROM events WHERE id = ?";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Event event = new Event(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("event_date").toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getInt("capacity")
                    );
                    event.setCurrentAttendees(rs.getInt("current_attendees"));
                    event.setStatus(rs.getString("status"));
                    return event;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving event by ID: " + e.getMessage());
        }
        return null;
    }
    
    // Update event
    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET name = ?, event_date = ?, location = ?, description = ?, capacity = ?, current_attendees = ?, status = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, event.getName());
            pstmt.setDate(2, Date.valueOf(event.getDate()));
            pstmt.setString(3, event.getLocation());
            pstmt.setString(4, event.getDescription());
            pstmt.setInt(5, event.getCapacity());
            pstmt.setInt(6, event.getCurrentAttendees());
            pstmt.setString(7, event.getStatus());
            pstmt.setInt(8, event.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
        }
        return false;
    }
    
    // Delete event
    public boolean deleteEvent(int id) {
        String sql = "DELETE FROM events WHERE id = ?";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
        }
        return false;
    }
    
    // Search events by name
    public List<Event> searchEventsByName(String name) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE name LIKE ? ORDER BY event_date ASC";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Event event = new Event(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("event_date").toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getInt("capacity")
                    );
                    event.setCurrentAttendees(rs.getInt("current_attendees"));
                    event.setStatus(rs.getString("status"));
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching events by name: " + e.getMessage());
        }
        return events;
    }
    
    // Search events by location
    public List<Event> searchEventsByLocation(String location) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE location LIKE ? ORDER BY event_date ASC";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Event event = new Event(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("event_date").toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getInt("capacity")
                    );
                    event.setCurrentAttendees(rs.getInt("current_attendees"));
                    event.setStatus(rs.getString("status"));
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching events by location: " + e.getMessage());
        }
        return events;
    }
    
    // Search events by status
    public List<Event> searchEventsByStatus(String status) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE status = ? ORDER BY event_date ASC";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Event event = new Event(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("event_date").toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getInt("capacity")
                    );
                    event.setCurrentAttendees(rs.getInt("current_attendees"));
                    event.setStatus(rs.getString("status"));
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching events by status: " + e.getMessage());
        }
        return events;
    }
    
    // Search events by date range
    public List<Event> searchEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE event_date BETWEEN ? AND ? ORDER BY event_date ASC";
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Event event = new Event(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("event_date").toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getInt("capacity")
                    );
                    event.setCurrentAttendees(rs.getInt("current_attendees"));
                    event.setStatus(rs.getString("status"));
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching events by date range: " + e.getMessage());
        }
        return events;
    }
    
    // Get event statistics
    public EventStatistics getEventStatistics() {
        String sql = """
            SELECT 
                COUNT(*) as total_events,
                SUM(CASE WHEN status = 'PLANNED' THEN 1 ELSE 0 END) as planned_events,
                SUM(CASE WHEN status = 'ONGOING' THEN 1 ELSE 0 END) as ongoing_events,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_events,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_events,
                SUM(capacity) as total_capacity,
                SUM(current_attendees) as total_attendees
            FROM events
        """;
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return new EventStatistics(
                    rs.getInt("total_events"),
                    rs.getInt("planned_events"),
                    rs.getInt("ongoing_events"),
                    rs.getInt("completed_events"),
                    rs.getInt("cancelled_events"),
                    rs.getInt("total_capacity"),
                    rs.getInt("total_attendees")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting event statistics: " + e.getMessage());
        }
        return new EventStatistics(0, 0, 0, 0, 0, 0, 0);
    }
    
    // Inner class for statistics
    public static class EventStatistics {
        public final int totalEvents;
        public final int plannedEvents;
        public final int ongoingEvents;
        public final int completedEvents;
        public final int cancelledEvents;
        public final int totalCapacity;
        public final int totalAttendees;
        
        public EventStatistics(int totalEvents, int plannedEvents, int ongoingEvents, 
                             int completedEvents, int cancelledEvents, 
                             int totalCapacity, int totalAttendees) {
            this.totalEvents = totalEvents;
            this.plannedEvents = plannedEvents;
            this.ongoingEvents = ongoingEvents;
            this.completedEvents = completedEvents;
            this.cancelledEvents = cancelledEvents;
            this.totalCapacity = totalCapacity;
            this.totalAttendees = totalAttendees;
        }
        
        public double getOccupancyRate() {
            return totalCapacity > 0 ? (double) totalAttendees / totalCapacity * 100 : 0;
        }
    }
}
