package dk.agrisys.pigfeedingsystem.model;

import dk.agrisys.pigfeedingsystem.Generator;
import dk.agrisys.pigfeedingsystem.dao.InviteCodeDAO;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class User {
    private String username;
    private String password; // NB: Gem ALDRIG passwords i klartekst i et rigtigt system!
    private UserRole role;
    private int userId;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = Integer.parseInt(Generator.generate(16));
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; } // Kun til simpel demo
    public UserRole getRole() { return role; }
    public int getId(){return userId;}
}