package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;
import java.util.HashMap;
import java.util.Map;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class UserDAO {

    // Simpel in-memory "database" til demo
    private static final Map<String, User> users = new HashMap<>();

    static {
        // Opret et par testbrugere
        users.put("super", new User("super", "pass1", UserRole.SUPERUSER));
        users.put("user", new User("user", "pass2", UserRole.USER));
    }

    // Simulerer opslag i databasen
    public User findUserByUsername(String username) {
        System.out.println("DAO: Finder bruger: " + username + " (MOCK)");
        return users.get(username); // Returnerer null hvis brugeren ikke findes
    }

    // Tilføj metoder til at oprette/opdatere brugere senere
}