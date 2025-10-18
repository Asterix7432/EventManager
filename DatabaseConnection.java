import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/eventmanager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
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
            
            // Try to load MySQL JDBC driver if available
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException driverEx) {
                System.err.println("MySQL JDBC Driver not found in classpath.");
                System.err.println("Continuing without DB connectivity. Some features will be disabled.");
                connection = null;
                return;
            }
            
            // Attempt primary connection; if DB missing, fall back to server-level connection, bootstrap schema, and continue
            try {
                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException primaryEx) {
                if (isUnknownDatabaseError(primaryEx)) {
                    String baseUrl = deriveServerUrl(url);
                    System.err.println("Database 'eventmanager' not found. Attempting to create it...");
                    try {
                        connection = DriverManager.getConnection(baseUrl, user, password);
                        createDatabaseIfNotExists();
                        createTableIfNotExists();
                        System.out.println("Database and tables created successfully.");
                    } catch (SQLException bootstrapEx) {
                        System.err.println("Failed to bootstrap database: " + bootstrapEx.getMessage());
                        connection = null;
                        return;
                    }
                } else {
                    throw primaryEx;
                }
            }
            
            // Ensure schema exists even if primary connection succeeded (idempotent)
            try {
                createDatabaseIfNotExists();
                createTableIfNotExists();
            } catch (SQLException ensureEx) {
                System.err.println("Schema ensure failed: " + ensureEx.getMessage());
            }
            
            System.out.println("Database connection established successfully!");
            
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("Please ensure MySQL is running and the database credentials are correct.");
            connection = null;
        }
    }

    private boolean isUnknownDatabaseError(SQLException e) {
        // MySQL error code 1049, SQLState 42000 for unknown database
        return ("42000".equals(e.getSQLState()) || e.getErrorCode() == 1049) &&
               (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown database"));
    }

    private String deriveServerUrl(String url) {
        // Convert jdbc:mysql://host:port/db?params -> jdbc:mysql://host:port/?params
        int qm = url.indexOf('?');
        String params = qm >= 0 ? url.substring(qm) : "";
        String withoutParams = qm >= 0 ? url.substring(0, qm) : url;
        int lastSlash = withoutParams.lastIndexOf('/');
        if (lastSlash > "jdbc:mysql://".length() - 1) {
            return withoutParams.substring(0, lastSlash + 1) + params; // keep trailing slash
        }
        return url; // fallback; shouldn't happen
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
