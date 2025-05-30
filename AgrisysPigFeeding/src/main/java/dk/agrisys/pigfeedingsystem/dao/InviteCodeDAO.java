package dk.agrisys.pigfeedingsystem.dao;

import dk.util.SessionContext;
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.service.UserService;

import java.sql.*;
import java.util.UUID;

/**
 * DAO class for handling invite codes.
 * Contains methods to generate, save, and retrieve invite codes from the database.
 */
public class InviteCodeDAO {

    public static final String INITIAL_CODE = "0123456789"; // Default code for the initial admin user

    /**
     * Generates a unique invite code.
     * @return A new UUID-based code as a string.
     */
    public String generateCode() {
        return UUID.randomUUID().toString();
    }

    /**
     * Saves an invite code to the database.
     * @param code The invite code to save.
     * @param isAdmin Indicates whether the code is for an admin user.
     * @return True if the code was saved successfully, otherwise false.
     * @throws SQLException If an error occurs during the database operation.
     */
    public boolean saveCodeToDb(String code, boolean isAdmin) throws SQLException {
        UserDAO userDAO = new UserDAO();
        boolean userExists = userDAO.validateUserID(Integer.parseInt(SessionContext.getCurrentUser().getId()));
        if (!userExists) {
            System.out.println("USER ID DOES NOT EXIST!");
        }
        System.out.println("USERID: " + SessionContext.getCurrentUser().getId());

        String query = "INSERT INTO Invites (Code, CreatedAt, UsedBy, isAdmin, CreatedBy) VALUES(?, CAST(? AS DATETIME2), NULL, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");
                return false;
            }
            Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // Get the current timestamp
            User user = SessionContext.getCurrentUser();
            pstmt.setLong(1, Long.parseLong(code));
            pstmt.setTimestamp(2, timestamp);
            pstmt.setBoolean(3, isAdmin);
            pstmt.setInt(4, Integer.parseInt(user.getId()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if at least one row was affected

        } catch (SQLException e) {
            System.out.println("DAO (DB): Failed to save invite code to database: " + e.getMessage());
        }

        return false;
    }

    /**
     * Creates an initial admin user.
     * Used to create a default admin user with a predefined code.
     */
    public void createInitialUser() {
        UserService userService = new UserService();
        userService.createUser("admin", "admin", INITIAL_CODE);
    }

    /**
     * Creates an initial admin invite code in the database.
     */
    public void createInitialCode() {
        String query = "INSERT INTO Invites (Code, isAdmin, CreatedAt, UsedBy, CreatedBy) VALUES(?, ?, CAST(? AS DATETIME2), NULL, NULL)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");
                return;
            }
            Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // Get the current timestamp
            pstmt.setLong(1, Long.parseLong(INITIAL_CODE)); // Initial admin code is 0123456789
            pstmt.setBoolean(2, true); // Initial code is for admin
            pstmt.setTimestamp(3, timestamp);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("DAO (DB): Failed to save invite code to database: " + e.getMessage());
        }
    }

    /**
     * Retrieves the role (admin or not) for an invite code.
     * @param code The invite code to check.
     * @return True if the code is for an admin user, otherwise false.
     */
    public boolean getInviteCodeRoleType(String code) {
        String query = "SELECT isAdmin FROM Invites WHERE Code = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");
                return false;
            }
            pstmt.setLong(1, Long.parseLong(code));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("isAdmin");
                }
            }

        } catch (SQLException e) {
            System.out.println("DAO (DB): Failed to retrieve invite code role type: " + e.getMessage());
        }
        return false;
    }
}