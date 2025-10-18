import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AttendeeManagementDialog extends JDialog {
    private Event event;
    private EventDAO eventDAO;
    private JLabel eventInfoLabel;
    private JLabel attendeesLabel;
    private JLabel capacityLabel;
    private JLabel availableLabel;
    private JSpinner addSpinner;
    private JSpinner removeSpinner;
    
    public AttendeeManagementDialog(JFrame parent, String title, boolean modal, Event event, EventDAO eventDAO) {
        super(parent, title, modal);
        this.event = event;
        this.eventDAO = eventDAO;
        initializeComponents();
        layoutComponents();
        updateDisplay();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        eventInfoLabel = new JLabel();
        eventInfoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        attendeesLabel = new JLabel();
        capacityLabel = new JLabel();
        availableLabel = new JLabel();
        
        addSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        removeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Event info
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(eventInfoLabel, gbc);
        
        // Current status
        gbc.gridwidth = 1; gbc.gridy = 1;
        mainPanel.add(new JLabel("Current Attendees:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(attendeesLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(capacityLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Available Spots:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(availableLabel, gbc);
        
        // Add attendees section
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Add Attendees:"), gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 5;
        mainPanel.add(new JLabel("Number to add:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(addSpinner, gbc);
        
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addAttendees();
            }
        });
        gbc.gridx = 2;
        mainPanel.add(addButton, gbc);
        
        // Remove attendees section
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Remove Attendees:"), gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 7;
        mainPanel.add(new JLabel("Number to remove:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(removeSpinner, gbc);
        
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeAttendees();
            }
        });
        gbc.gridx = 2;
        mainPanel.add(removeButton, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateDisplay() {
        eventInfoLabel.setText(event.getName() + " (" + event.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ")");
        attendeesLabel.setText(String.valueOf(event.getCurrentAttendees()));
        capacityLabel.setText(String.valueOf(event.getCapacity()));
        availableLabel.setText(String.valueOf(event.getAvailableSpots()));
        
        // Update spinner limits
        int maxAdd = event.getAvailableSpots();
        int maxRemove = event.getCurrentAttendees();
        
        addSpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(1, maxAdd), 1));
        removeSpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(1, maxRemove), 1));
        
        addSpinner.setEnabled(maxAdd > 0);
        removeSpinner.setEnabled(maxRemove > 0);
    }
    
    private void addAttendees() {
        int toAdd = (Integer) addSpinner.getValue();
        int available = event.getAvailableSpots();
        
        if (toAdd > available) {
            JOptionPane.showMessageDialog(this, 
                "Cannot add " + toAdd + " attendees. Only " + available + " spots available.",
                "Insufficient Capacity", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        for (int i = 0; i < toAdd; i++) {
            event.addAttendee();
        }
        
        // Update in database
        if (eventDAO.updateEvent(event)) {
            updateDisplay();
            JOptionPane.showMessageDialog(this, 
                "Successfully added " + toAdd + " attendees.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to update attendees in database.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeAttendees() {
        int toRemove = (Integer) removeSpinner.getValue();
        int current = event.getCurrentAttendees();
        
        if (toRemove > current) {
            JOptionPane.showMessageDialog(this, 
                "Cannot remove " + toRemove + " attendees. Only " + current + " attendees registered.",
                "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        for (int i = 0; i < toRemove; i++) {
            event.removeAttendee();
        }
        
        // Update in database
        if (eventDAO.updateEvent(event)) {
            updateDisplay();
            JOptionPane.showMessageDialog(this, 
                "Successfully removed " + toRemove + " attendees.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to update attendees in database.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
