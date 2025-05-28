package dk.agrisys.pigfeedingsystem.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The `DatabaseConnector` class handles the connection to the database.
 * It provides methods to establish and test the connection.
 */
public class DatabaseConnector {

    // --- Database connection details (SQL SERVER EXAMPLE) ---
    // These details should be retrieved from a secure configuration to protect sensitive data.
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Agrisys;encrypt=true;trustServerCertificate=true;"; // Database URL
    private static final String DB_USER = "Magnus"; // Database username
    private static final String DB_PASSWORD = "Jpc77rfc"; // Database password

    /**
     * Establishes a connection to the database.
     * @return A `Connection` object representing the connection.
     * @throws SQLException If an error occurs while connecting.
     */
    public static Connection getConnection() throws SQLException {
        // Returns a new connection to the database using DriverManager.
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Tests the connection to the database.
     * This method can be used to verify that the driver and connection details are correct.
     */
    public static void testConnection() {
        Connection conn = null;
        try {
            // Ensures that the MSSQL JDBC driver is available.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("MSSQL Driver found.");

            // Attempts to establish a connection to the database.
            conn = getConnection();
            if (conn != null) {
                System.out.println("Database connection successful!");
                conn.close(); // Closes the connection after testing.
            } else {
                System.out.println("Database connection failed (getConnection returned null).");
            }
        } catch (ClassNotFoundException e) {
            // Error if the JDBC driver is not found.
            System.err.println("MSSQL JDBC Driver NOT found! Add dependency to pom.xml.");
            e.printStackTrace();
        } catch (SQLException e) {
            // Error while attempting to connect.
            System.err.println("Could not connect to the database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensures the connection is closed if it was established.
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}