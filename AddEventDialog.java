import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddEventDialog extends JDialog {
    private JTextField nameField;
    private JTextField dateField;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JSpinner capacitySpinner;
    private JComboBox<String> statusComboBox;
    private boolean eventAdded = false;
    private EventDAO eventDAO;
    
    public AddEventDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
        this.eventDAO = new EventDAO();
        initializeComponents();
        layoutComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        nameField = new JTextField(20);
        dateField = new JTextField(20);
        locationField = new JTextField(20);
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        capacitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        statusComboBox = new JComboBox<>(new String[]{"PLANNED", "ONGOING", "COMPLETED", "CANCELLED"});
        
        // Set default date to today
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Event Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Event Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        // Date
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);
        
        // Location
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        formPanel.add(locationField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Capacity
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(capacitySpinner, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusComboBox, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Event");
        JButton cancelButton = new JButton("Cancel");
        
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateAndAddEvent()) {
                    eventAdded = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private boolean validateAndAddEvent() {
        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Event name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // Validate date
        LocalDate eventDate;
        try {
            eventDate = LocalDate.parse(dateField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            dateField.requestFocus();
            return false;
        }
        
        // Validate location
        if (locationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            locationField.requestFocus();
            return false;
        }
        
        // Validate capacity
        int capacity = (Integer) capacitySpinner.getValue();
        if (capacity <= 0) {
            JOptionPane.showMessageDialog(this, "Capacity must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            capacitySpinner.requestFocus();
            return false;
        }
        
        // Create event
        try {
            Event newEvent = new Event(
                0, // ID will be set by database
                nameField.getText().trim(),
                dateField.getText().trim(),
                locationField.getText().trim(),
                descriptionArea.getText().trim(),
                capacity
            );
            
            // Set status if not PLANNED
            String selectedStatus = (String) statusComboBox.getSelectedItem();
            if (!selectedStatus.equals("PLANNED")) {
                newEvent.setStatus(selectedStatus);
            }
            
            // Save to database
            if (eventDAO.createEvent(newEvent)) {
                JOptionPane.showMessageDialog(this, "Event added successfully with ID: " + newEvent.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add event to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating event: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean isEventAdded() {
        return eventAdded;
    }
}
