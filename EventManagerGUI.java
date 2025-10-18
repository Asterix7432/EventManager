import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventManagerGUI extends JFrame {
    private EventDAO eventDAO;
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    
    public EventManagerGUI() {
        eventDAO = new EventDAO();
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Event Management System (Database)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Use default look and feel
        
        createMenuBar();
        createMainPanel();
        
        setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            DatabaseConnection.getInstance().closeConnection();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        
        // Event Menu
        JMenu eventMenu = new JMenu("Event");
        JMenuItem addEventItem = new JMenuItem("Add New Event");
        JMenuItem editEventItem = new JMenuItem("Edit Selected Event");
        JMenuItem deleteEventItem = new JMenuItem("Delete Selected Event");
        
        addEventItem.addActionListener(e -> showAddEventDialog());
        editEventItem.addActionListener(e -> editSelectedEvent());
        deleteEventItem.addActionListener(e -> deleteSelectedEvent());
        
        eventMenu.add(addEventItem);
        eventMenu.add(editEventItem);
        eventMenu.add(deleteEventItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        JMenuItem reportItem = new JMenuItem("Generate Report");
        
        refreshItem.addActionListener(e -> refreshEventTable());
        reportItem.addActionListener(e -> showReportDialog());
        
        viewMenu.add(refreshItem);
        viewMenu.add(reportItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(eventMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainPanel() {
        setLayout(new BorderLayout());
        
        // Top Panel - Search and Filter
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center Panel - Event Table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom Panel - Buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        
        // Search field
        panel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addActionListener(e -> filterEvents());
        panel.add(searchField);
        
        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> filterEvents());
        panel.add(searchButton);
        
        // Status filter
        panel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(new String[]{"All", "PLANNED", "ONGOING", "COMPLETED", "CANCELLED"});
        statusFilter.addActionListener(e -> filterEvents());
        panel.add(statusFilter);
        
        // Clear filter button
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            filterEvents();
        });
        panel.add(clearButton);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Events"));
        
        // Create table
        String[] columnNames = {"ID", "Name", "Date", "Location", "Capacity", "Attendees", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        eventTable = new JTable(tableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setRowHeight(25);
        
        // Set column widths
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Date
        eventTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Location
        eventTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Capacity
        eventTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Attendees
        eventTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Status
        
        JScrollPane scrollPane = new JScrollPane(eventTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Add Event");
        JButton editButton = new JButton("Edit Event");
        JButton deleteButton = new JButton("Delete Event");
        JButton attendeesButton = new JButton("Manage Attendees");
        JButton reportButton = new JButton("Generate Report");
        
        addButton.addActionListener(e -> showAddEventDialog());
        editButton.addActionListener(e -> editSelectedEvent());
        deleteButton.addActionListener(e -> deleteSelectedEvent());
        attendeesButton.addActionListener(e -> manageAttendees());
        reportButton.addActionListener(e -> showReportDialog());
        
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(attendeesButton);
        panel.add(reportButton);
        
        return panel;
    }
    
    private void showAddEventDialog() {
        AddEventDialog dialog = new AddEventDialog(this, "Add New Event", true);
        dialog.setVisible(true);
        
        if (dialog.isEventAdded()) {
            refreshEventTable();
        }
    }
    
    private void editSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int eventId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Event eventToEdit = eventDAO.getEventById(eventId);
        
        if (eventToEdit != null) {
            EditEventDialog dialog = new EditEventDialog(this, "Edit Event", true, eventToEdit);
            dialog.setVisible(true);
            refreshEventTable();
        }
    }
    
    private void deleteSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int eventId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Event eventToDelete = eventDAO.getEventById(eventId);
        
        if (eventToDelete != null) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this event?\n\n" + 
                "Event: " + eventToDelete.getName() + "\n" +
                "Date: " + eventToDelete.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                if (eventDAO.deleteEvent(eventId)) {
                    JOptionPane.showMessageDialog(this, "Event deleted successfully!");
                    refreshEventTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete event.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void manageAttendees() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to manage attendees.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int eventId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Event event = eventDAO.getEventById(eventId);
        
        if (event != null) {
            AttendeeManagementDialog dialog = new AttendeeManagementDialog(this, "Manage Attendees", true, event, eventDAO);
            dialog.setVisible(true);
            refreshEventTable();
        }
    }
    
    private void showReportDialog() {
        ReportDialog dialog = new ReportDialog(this, "Event Report", true, eventDAO);
        dialog.setVisible(true);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, 
            "Event Management System (Database)\nVersion 1.0\n\n" +
            "A comprehensive system for managing events,\n" +
            "attendees, and generating reports with MySQL database.",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void refreshEventTable() {
        tableModel.setRowCount(0);
        List<Event> events = eventDAO.getAllEvents();
        
        for (Event event : events) {
            Object[] row = {
                event.getId(),
                event.getName(),
                event.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                event.getLocation(),
                event.getCapacity(),
                event.getCurrentAttendees(),
                event.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void filterEvents() {
        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        
        tableModel.setRowCount(0);
        List<Event> events;
        
        if (selectedStatus.equals("All")) {
            events = eventDAO.getAllEvents();
        } else {
            events = eventDAO.searchEventsByStatus(selectedStatus);
        }
        
        for (Event event : events) {
            boolean matchesSearch = searchText.isEmpty() || 
                event.getName().toLowerCase().contains(searchText) ||
                event.getLocation().toLowerCase().contains(searchText);
            
            if (matchesSearch) {
                Object[] row = {
                    event.getId(),
                    event.getName(),
                    event.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    event.getLocation(),
                    event.getCapacity(),
                    event.getCurrentAttendees(),
                    event.getStatus()
                };
                tableModel.addRow(row);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventManagerGUI());
    }
}
