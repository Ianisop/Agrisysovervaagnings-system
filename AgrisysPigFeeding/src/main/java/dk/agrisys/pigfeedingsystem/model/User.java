package dk.agrisys.pigfeedingsystem.model;

import dk.agrisys.pigfeedingsystem.Generator;

public class User {
    private String username;
    private String password; // NB: Never store passwords in plain text in a real system!
    private UserRole role;
    private String userId; // Changed to String to handle non-integer IDs

    public User(String username, String password, UserRole role, String userId) {
        this.username = username;
        this.password = password;
        this.role = role;


        // Generate a valid ID as a String
        this.userId = userId; // Ensure the generator produces a valid unique string
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password; // For demo purposes only
    }

    public UserRole getRole() {
        return role;
    }

    public String getId() {
        return userId;
    }
}