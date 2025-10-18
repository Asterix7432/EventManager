import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportDialog extends JDialog {
    private EventDAO eventDAO;
    
    public ReportDialog(JFrame parent, String title, boolean modal, EventDAO eventDAO) {
        super(parent, title, modal);
        this.eventDAO = eventDAO;
        initializeComponents();
        layoutComponents();
        generateReport();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Components will be created in layoutComponents
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create scrollable text area for report
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        JButton exportButton = new JButton("Export to File");
        
        closeButton.addActionListener(e -> dispose());
        exportButton.addActionListener(e -> exportReport(reportArea.getText()));
        
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Store reference to report area for report generation
        this.reportArea = reportArea;
    }
    
    private JTextArea reportArea;
    
    private void generateReport() {
        StringBuilder report = new StringBuilder();
        
        // Header
        report.append("=".repeat(60)).append("\n");
        report.append("                    EVENT MANAGEMENT REPORT (DATABASE)\n");
        report.append("=".repeat(60)).append("\n");
        report.append("Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("\n\n");
        
        // Get statistics from database
        EventDAO.EventStatistics stats = eventDAO.getEventStatistics();
        
        // Summary Statistics
        report.append("SUMMARY STATISTICS\n");
        report.append("-".repeat(30)).append("\n");
        report.append(String.format("Total Events: %d\n", stats.totalEvents));
        
        if (stats.totalEvents == 0) {
            report.append("\nNo events found in database.\n");
            reportArea.setText(report.toString());
            return;
        }
        
        // Status breakdown
        report.append(String.format("Planned Events: %d\n", stats.plannedEvents));
        report.append(String.format("Ongoing Events: %d\n", stats.ongoingEvents));
        report.append(String.format("Completed Events: %d\n", stats.completedEvents));
        report.append(String.format("Cancelled Events: %d\n", stats.cancelledEvents));
        
        // Capacity statistics
        report.append("\nCAPACITY STATISTICS\n");
        report.append("-".repeat(30)).append("\n");
        report.append(String.format("Total Capacity: %d\n", stats.totalCapacity));
        report.append(String.format("Total Attendees: %d\n", stats.totalAttendees));
        report.append(String.format("Overall Occupancy: %.1f%%\n", stats.getOccupancyRate()));
        
        // Event details
        report.append("\nEVENT DETAILS\n");
        report.append("-".repeat(30)).append("\n");
        
        List<Event> events = eventDAO.getAllEvents();
        for (Event event : events) {
            report.append(String.format("ID: %d\n", event.getId()));
            report.append(String.format("Name: %s\n", event.getName()));
            report.append(String.format("Date: %s\n", event.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            report.append(String.format("Location: %s\n", event.getLocation()));
            report.append(String.format("Capacity: %d\n", event.getCapacity()));
            report.append(String.format("Attendees: %d\n", event.getCurrentAttendees()));
            report.append(String.format("Available: %d\n", event.getAvailableSpots()));
            report.append(String.format("Status: %s\n", event.getStatus()));
            report.append(String.format("Description: %s\n", event.getDescription()));
            report.append("-".repeat(30)).append("\n");
        }
        
        // Top events by attendance
        report.append("\nTOP EVENTS BY ATTENDANCE\n");
        report.append("-".repeat(30)).append("\n");
        
        events.sort((e1, e2) -> Integer.compare(e2.getCurrentAttendees(), e1.getCurrentAttendees()));
        
        int count = 0;
        for (Event event : events) {
            if (count >= 5) break; // Top 5
            double percentage = event.getCapacity() > 0 ? 
                (double) event.getCurrentAttendees() / event.getCapacity() * 100 : 0;
            report.append(String.format("%d. %s - %d/%d (%.1f%%)\n", 
                count + 1, event.getName(), event.getCurrentAttendees(), 
                event.getCapacity(), percentage));
            count++;
        }
        
        // Events by status
        report.append("\nEVENTS BY STATUS\n");
        report.append("-".repeat(30)).append("\n");
        
        for (String status : new String[]{"PLANNED", "ONGOING", "COMPLETED", "CANCELLED"}) {
            report.append(String.format("\n%s Events:\n", status));
            List<Event> statusEvents = eventDAO.searchEventsByStatus(status);
            if (statusEvents.isEmpty()) {
                report.append("  No events found.\n");
            } else {
                for (Event event : statusEvents) {
                    report.append(String.format("  - %s (%s)\n", 
                        event.getName(), 
                        event.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
                }
            }
        }
        
        report.append("\n" + "=".repeat(60)).append("\n");
        report.append("End of Report\n");
        
        reportArea.setText(report.toString());
    }
    
    private void exportReport(String reportText) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setSelectedFile(new java.io.File("event_report_database.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.nio.file.Files.write(file.toPath(), reportText.getBytes());
                JOptionPane.showMessageDialog(this, 
                    "Report exported successfully to: " + file.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting report: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
