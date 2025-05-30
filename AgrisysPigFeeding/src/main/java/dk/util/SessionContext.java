package dk.util;

import dk.agrisys.pigfeedingsystem.model.User;

public class SessionContext {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser(){
        return currentUser;
    }
}
