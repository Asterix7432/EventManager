import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/eventmanager";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    private static final String CONFIG_FILE = "database.properties";
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection() {
        initializeDatabase();
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try {
            // Load database configuration from properties file
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(CONFIG_FILE));
                System.out.println("Loaded database configuration from " + CONFIG_FILE);
            } catch (IOException e) {
                // Use default configuration if file doesn't exist
                System.out.println("No database.properties file found, using default configuration");
            }
            
            String url = props.getProperty("db.url", DB_URL);
            String user = props.getProperty("db.user", DB_USER);
            String password = props.getProperty("db.password", DB_PASSWORD);
            
            System.out.println("Attempting to connect to database: " + url);
            
            // Load MySQL JDBC driver - MANDATORY
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("MySQL JDBC Driver loaded successfully");
            } catch (ClassNotFoundException driverEx) {
                String errorMsg = "FATAL ERROR: MySQL JDBC Driver not found in classpath!\n" +
                                 "Please ensure mysql-connector-j.jar is in the classpath.\n" +
                                 "The application cannot run without database connectivity.";
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg, driverEx);
            }
            
            // Create connection - MANDATORY
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connection established successfully!");
            
            // Create database and table if they don't exist
            createDatabaseIfNotExists();
            createTableIfNotExists();
            
            System.out.println("Database initialization complete!");
            
        } catch (SQLException e) {
            String errorMsg = "FATAL ERROR: Database connection failed!\n" +
                             "Error: " + e.getMessage() + "\n" +
                             "Please ensure:\n" +
                             "1. MySQL server is running\n" +
                             "2. Database credentials in database.properties are correct\n" +
                             "3. Database 'eventmanager' exists or can be created\n" +
                             "The application cannot run without database connectivity.";
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    private void createDatabaseIfNotExists() throws SQLException {
        String createDbSQL = "CREATE DATABASE IF NOT EXISTS eventmanager";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createDbSQL);
        }
        
        // Use the database
        String useDbSQL = "USE eventmanager";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(useDbSQL);
        }
    }
    
    private void createTableIfNotExists() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS events (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(255) NOT NULL,
                event_date DATE NOT NULL,
                location VARCHAR(255) NOT NULL,
                description TEXT,
                capacity INT NOT NULL,
                current_attendees INT DEFAULT 0,
                status ENUM('PLANNED', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'PLANNED',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String errorMsg = "FATAL ERROR: Database connection is not available!";
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        } catch (SQLException e) {
            String errorMsg = "FATAL ERROR: Cannot verify database connection: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Test database connection
    public static void main(String[] args) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        if (db.isConnected()) {
            System.out.println("Database connection test successful!");
        } else {
            System.out.println("Database connection test failed!");
        }
        db.closeConnection();
    }
}
