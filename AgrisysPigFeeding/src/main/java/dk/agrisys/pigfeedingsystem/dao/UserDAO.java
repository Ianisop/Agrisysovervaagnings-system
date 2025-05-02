package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {

    // --- Mock In-Memory "Database" ---
    // This is a simulated database for testing purposes. It can be removed when using a real database.
    private static final Map<String, User> mockUsers = new HashMap<>();
    static {
        // Adding mock users for testing
        mockUsers.put("super", new User("super", "pass1", UserRole.SUPERUSER));
        mockUsers.put("user", new User("user", "pass2", UserRole.USER));
        System.out.println("DAO: Mock users initialized (INSECURE).");
    }

    // Mock method to find a user by username (used for testing without a real database)
    public User findUserByUsername(String username) {
        return mockUsers.get(username);
    }

    // Mock method to create a user (used for testing without a real database)
    public boolean createUser(String u, String p, UserRole r) {
        return false;
    }

    // === Method: Register a new user in the database ===
    /**
     * Registers a new user in the SQL Server database using DatabaseConnector.
     * Passwords are hashed using BCrypt, and PreparedStatement is used for security.
     *
     * @param username The username of the new user.
     * @param plainPassword The plain-text password of the new user.
     * @param role The role of the new user (e.g., USER or SUPERUSER).
     * @return true if the user was successfully registered, false otherwise.
     */
    public boolean registerUserInDb(String username, String plainPassword, UserRole role) {
        // Validate input parameters
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty() || role == null) {
            System.err.println("DAO (DB): Invalid input for registerUserInDb.");
            return false;
        }

        // Hash the password using BCrypt
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // Map UserRole to a numeric RoleID for the database
        int roleId;
        switch (role) {
            case USER: roleId = 1; break;
            case SUPERUSER: roleId = 2; break;
            default:
                System.err.println("DAO (DB): Unknown role: " + role);
                return false;
        }

        // SQL query to insert a new user into the database
        String sql = "INSERT INTO [User] (Username, PasswordHash, RoleID) VALUES (?, ?, ?)";

        // Use DatabaseConnector to establish a connection and execute the query
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Check if the connection was successfully established
            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");
                return false;
            }

            // Set the query parameters
            pstmt.setString(1, username.trim());
            pstmt.setString(2, hashedPassword);
            pstmt.setInt(3, roleId);

            // Execute the query and check if rows were affected
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DAO (DB): User '" + username + "' successfully registered.");
                return true;
            } else {
                System.err.println("DAO (DB): No rows affected for '" + username + "'.");
                return false;
            }

        } catch (SQLException e) {
            // Handle SQL exceptions, including duplicate key errors
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                System.err.println("DAO (DB): Username '" + username + "' already exists.");
            } else {
                System.err.println("DAO (DB): SQL error during registration of '" + username + "': " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("DAO (DB): Unexpected error during registration of '" + username + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // === Method: Verify user login credentials ===
    /**
     * Verifies the user's login credentials against the database.
     *
     * @param username The username of the user attempting to log in.
     * @param plainPassword The plain-text password provided by the user.
     * @return A User object if the login is successful, or null if it fails.
     */
    public User verifyUserLoginFromDb(String username, String plainPassword) {
        // SQL query to retrieve user details by username
        String sql = "SELECT UserID, Username, PasswordHash, RoleID FROM [User] WHERE Username = ?";
        User user = null;

        // Use DatabaseConnector to establish a connection and execute the query
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Check if the connection was successfully established
            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection for login verification.");
                return null;
            }

            // Set the query parameter
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            // Check if a user was found
            if (rs.next()) {
                String storedHash = rs.getString("PasswordHash");

                // Verify the password using BCrypt
                if (BCrypt.checkpw(plainPassword, storedHash)) {
                    // Password is correct, create a User object
                    int userId = rs.getInt("UserID");
                    int roleId = rs.getInt("RoleID");
                    UserRole role = (roleId == 2) ? UserRole.SUPERUSER : UserRole.USER;

                    // Create a User object (password is not stored in the object for security)
                    user = new User(username, null, role);
                    System.out.println("DAO (DB): Login successful for: " + username);
                } else {
                    System.out.println("DAO (DB): Incorrect password for: " + username);
                }
            } else {
                System.out.println("DAO (DB): User not found: " + username);
            }

        } catch (SQLException e) {
            // Handle SQL exceptions
            System.err.println("DAO (DB): SQL error during login verification for '" + username + "': " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("DAO (DB): Unexpected error during login verification for '" + username + "': " + e.getMessage());
            e.printStackTrace();
        }
        return user; // Return the User object or null
    }
}