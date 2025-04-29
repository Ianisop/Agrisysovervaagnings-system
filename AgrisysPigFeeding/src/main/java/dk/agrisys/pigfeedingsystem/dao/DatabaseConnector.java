package dk.agrisys.pigfeedingsystem.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Agrisys;encrypt=true;trustServerCertificate=true;";
    private static final String DB_USER = "Magnus";
    private static final String DB_PASSWORD = "Jpc77rfc";

    public static Connection getConnection() throws SQLException {
        System.out.println("Connecting to the database...");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Database connection successful!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}