import javax.swing.*;

/**
 * Main Entry Point for Event Management System
 * 
 * This is the primary entry point for the Event Management System.
 * It launches the Swing-based GUI application with database connectivity.
 * 
 * Prerequisites:
 * - MySQL Server must be running
 * - Database configured in database.properties
 * - MySQL Connector/J in classpath
 * 
 * @version 2.0
 */
public class Main {
    public static void main(String[] args) {
        // Print welcome banner
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   Event Management System - Version 2.0   ║");
        System.out.println("║        Database-Driven Application        ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println();
        
        // Verify database connection early
        try {
            System.out.println("Initializing database connection...");
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            
            if (!dbConnection.isConnected()) {
                showErrorAndExit("Database connection could not be established.\n" +
                               "Please check your MySQL server and database.properties file.");
                return;
            }
            
            System.out.println("Database connection verified successfully!");
            System.out.println("Starting GUI application...");
            System.out.println();
            
            // Launch GUI on Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                try {
                    // Set system look and feel for better native appearance
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // If setting look and feel fails, continue with default
                    System.out.println("Using default look and feel");
                }
                
                // Create and display the GUI
                new EventManagerGUI();
            });
            
        } catch (RuntimeException e) {
            // Database connection error
            String errorMessage = "Failed to initialize database connection!\n\n" +
                                "Error: " + e.getMessage() + "\n\n" +
                                "Please ensure:\n" +
                                "1. MySQL server is running\n" +
                                "2. Database credentials are correct in database.properties\n" +
                                "3. MySQL Connector/J is in the classpath";
            
            showErrorAndExit(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Unexpected error starting application!\n\n" +
                                "Error: " + e.getMessage();
            showErrorAndExit(errorMessage);
            e.printStackTrace();
        }
    }
    
    /**
     * Display error dialog and exit the application
     */
    private static void showErrorAndExit(String message) {
        System.err.println("\n" + "=".repeat(60));
        System.err.println("APPLICATION STARTUP FAILED");
        System.err.println("=".repeat(60));
        System.err.println(message);
        System.err.println("=".repeat(60));
        
        // Show GUI error dialog
        JOptionPane.showMessageDialog(
            null,
            message,
            "Event Management System - Startup Error",
            JOptionPane.ERROR_MESSAGE
        );
        
        System.exit(1);
    }
}

