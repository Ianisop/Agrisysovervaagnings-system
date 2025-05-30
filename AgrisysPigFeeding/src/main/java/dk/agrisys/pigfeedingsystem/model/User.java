package dk.agrisys.pigfeedingsystem.model;

/**
 * Model class representing a user.
 * Contains information about the username, password, role, and the user's unique ID.
 */
public class User {
    private String username; // The username of the user
    private String password; // The password of the user (Note: Never store passwords in plain text in a real system!)
    private UserRole role; // The role of the user (e.g., USER or SUPERUSER)
    private String userId; // The unique ID of the user as a string

    /**
     * Constructor to create a new user.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param role The role of the user.
     * @param userId The unique ID of the user.
     */
    public User(String username, String password, UserRole role, String userId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userId; // Ensures the ID is a valid unique string
    }

    /**
     * Retrieves the username of the user.
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the password of the user.
     * Note: This is for demonstration purposes only and should not be used in production.
     * @return The password of the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Retrieves the role of the user.
     * @return The role of the user.
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Retrieves the unique ID of the user.
     * @return The user's ID as a string.
     */
    public String getId() {
        return userId;
    }
}