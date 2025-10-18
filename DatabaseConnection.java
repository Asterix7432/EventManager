import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/eventmanager?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
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
            // Load database configuration from properties file if it exists
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(CONFIG_FILE));
            } catch (IOException e) {
                // Use default configuration if file doesn't exist
                System.out.println("No database.properties file found, using default configuration");
            }
            
            String url = props.getProperty("db.url", DB_URL);
            String user = props.getProperty("db.user", DB_USER);
            String password = props.getProperty("db.password", DB_PASSWORD);
            boolean fallbackToH2 = Boolean.parseBoolean(props.getProperty("db.fallbackToH2", "false"));
            String h2Url = props.getProperty("db.h2.url", "jdbc:h2:./.h2/eventmanager;MODE=MySQL;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE");
            String h2User = props.getProperty("db.h2.user", "sa");
            String h2Password = props.getProperty("db.h2.password", "");
            
            // Try to load MySQL JDBC driver if available
            boolean mysqlAvailable = false;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                mysqlAvailable = true;
            } catch (ClassNotFoundException driverEx) {
                System.err.println("MySQL JDBC Driver not found in classpath.");
            }

            SQLException lastError = null;
            if (mysqlAvailable) {
                try {
                    // First try direct connection (createDatabaseIfNotExist may create schema)
                    connection = DriverManager.getConnection(url, user, password);
                    // Ensure schema and tables
                    createDatabaseIfNotExists();
                    createTableIfNotExists();
                    System.out.println("Database connection (MySQL) established successfully!");
                    return;
                } catch (SQLException e) {
                    lastError = e;
                    System.err.println("MySQL connection attempt failed: " + e.getMessage());
                }
            }

            if (fallbackToH2) {
                try {
                    Class.forName("org.h2.Driver");
                    connection = DriverManager.getConnection(h2Url, h2User, h2Password);
                    createTableIfNotExists();
                    System.out.println("Database connection (H2 fallback) established successfully!");
                    return;
                } catch (ClassNotFoundException e) {
                    System.err.println("H2 JDBC Driver not found in classpath.");
                } catch (SQLException e) {
                    lastError = e;
                    System.err.println("H2 connection attempt failed: " + e.getMessage());
                }
            }

            // If we reach here, neither MySQL nor H2 connected
            if (lastError != null) {
                throw lastError;
            } else {
                throw new SQLException("No JDBC driver available for MySQL or H2.");
            }
            
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("Ensure MySQL is running or enable H2 fallback in database.properties.");
            e.printStackTrace();
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
                initializeDatabase();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
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
