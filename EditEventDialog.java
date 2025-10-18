import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EditEventDialog extends JDialog {
    private Event eventToEdit;
    private JTextField nameField;
    private JTextField dateField;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JSpinner capacitySpinner;
    private JComboBox<String> statusComboBox;
    private JSpinner attendeesSpinner;
    private EventDAO eventDAO;
    
    public EditEventDialog(JFrame parent, String title, boolean modal, Event event) {
        super(parent, title, modal);
        this.eventToEdit = event;
        this.eventDAO = new EventDAO();
        initializeComponents();
        layoutComponents();
        populateFields();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 450);
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
        attendeesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        statusComboBox = new JComboBox<>(new String[]{"PLANNED", "ONGOING", "COMPLETED", "CANCELLED"});
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
        
        // Current Attendees
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Current Attendees:"), gbc);
        gbc.gridx = 1;
        formPanel.add(attendeesSpinner, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusComboBox, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateAndSaveEvent()) {
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void populateFields() {
        nameField.setText(eventToEdit.getName());
        dateField.setText(eventToEdit.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        locationField.setText(eventToEdit.getLocation());
        descriptionArea.setText(eventToEdit.getDescription());
        capacitySpinner.setValue(eventToEdit.getCapacity());
        attendeesSpinner.setValue(eventToEdit.getCurrentAttendees());
        statusComboBox.setSelectedItem(eventToEdit.getStatus());
    }
    
    private boolean validateAndSaveEvent() {
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
        
        // Validate attendees
        int attendees = (Integer) attendeesSpinner.getValue();
        if (attendees < 0) {
            JOptionPane.showMessageDialog(this, "Attendees cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            attendeesSpinner.requestFocus();
            return false;
        }
        
        if (attendees > capacity) {
            JOptionPane.showMessageDialog(this, "Attendees cannot exceed capacity.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            attendeesSpinner.requestFocus();
            return false;
        }
        
        // Update event
        try {
            eventToEdit.setName(nameField.getText().trim());
            eventToEdit.setDate(dateField.getText().trim());
            eventToEdit.setLocation(locationField.getText().trim());
            eventToEdit.setDescription(descriptionArea.getText().trim());
            eventToEdit.setCapacity(capacity);
            eventToEdit.setCurrentAttendees(attendees);
            eventToEdit.setStatus((String) statusComboBox.getSelectedItem());
            
            // Save to database
            if (eventDAO.updateEvent(eventToEdit)) {
                JOptionPane.showMessageDialog(this, "Event updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update event in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating event: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
