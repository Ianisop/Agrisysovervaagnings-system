package dk.agrisys.pigfeedingsystem.dao; // <-- RET HVIS DIN PAKKE ER ANDERLEDES (f.eks. org.example.dao)

import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) for håndtering af Bruger-data i databasen.
 * Erstatter den tidligere HashMap-baserede mock-implementering.
 * Interagerer med 'Bruger' og 'Brugerrolle' tabellerne via JDBC.
 *
 * (Hvem har skrevet: [Dit Navn/Gruppens Navn]) // <--- UDFYLD DETTE
 */
public class UserDAO {

    /**
     * Finder en bruger i databasen baseret på det angivne brugernavn.
     * Henter også brugerens rolle ved at joine med Brugerrolle-tabellen.
     *
     * @param username Det brugernavn, der skal søges efter.
     * @return Et User-objekt hvis brugeren findes, ellers null.
     */
    public User findUserByUsername(String username) {
        // SQL-forespørgsel til at hente brugerdata og rollenavn.
        // ANTAGER: Brugerrolle tabel har kolonnerne RolleID og RolleNavn. RET HVIS NØDVENDIGT.
        String sql = "SELECT b.BrugerID, b.Brugernavn, b.Kodeord, br.RolleNavn " +
                "FROM Bruger b " +
                "JOIN Brugerrolle br ON b.RolleID = br.RolleID " +
                "WHERE b.Brugernavn = ?";
        User user = null; // Initialiser bruger til null

        System.out.println("DAO: Forsøger at finde bruger i DB: " + username);

        // Brug try-with-resources for at sikre korrekt lukning af ressourcer (Connection, PreparedStatement, ResultSet)
        try (Connection conn = DatabaseConnector.getConnection(); // Henter forbindelse fra den opdaterede DatabaseConnector
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username); // Sæt brugernavn parameter i SQL'en for at undgå SQL Injection

            // Eksekver forespørgslen og få resultatet
            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) { // Hvis der blev fundet en række (brugeren eksisterer)
                    String dbUsername = rs.getString("Brugernavn");
                    // !! SIKKERHEDSADVARSEL: Læser klartekst kodeord fra DB !!
                    String dbPassword = rs.getString("Kodeord");
                    String dbRoleName = rs.getString("RolleNavn"); // Hent rollenavnet fra JOIN
                    int userId = rs.getInt("BrugerID"); // Valgfrit at hente ID, men god praksis

                    System.out.println("DAO: Bruger fundet. Username: " + dbUsername + ", RolleNavn: " + dbRoleName);

                    try {
                        // Konverter rollenavnet (f.eks. "SUPERUSER") til den tilsvarende UserRole Enum værdi.
                        // toUpperCase() sikrer mod case-følsomhedsfejl i databasen.
                        UserRole role = UserRole.valueOf(dbRoleName.toUpperCase());

                        // Opret User objektet med data fra databasen
                        // !! Password gemmes her midlertidigt i klartekst i User objektet !!
                        user = new User(dbUsername, dbPassword, role);
                        // Hvis din User model har et id felt: user.setId(userId);

                        System.out.println("DAO: User objekt oprettet for: " + username);

                    } catch (IllegalArgumentException e) {
                        // Fejl hvis værdien i Brugerrolle.RolleNavn ikke matcher en UserRole enum værdi (SUPERUSER/USER)
                        System.err.println("DAO FEJL: Ukendt RolleNavn ('" + dbRoleName + "') fundet i Brugerrolle tabellen for bruger '" + username + "'. Tjek databasen og UserRole enum.");
                        e.printStackTrace();
                        // user forbliver null
                    }
                } else {
                    // Brugeren blev ikke fundet i databasen
                    System.out.println("DAO: Bruger IKKE fundet i DB: " + username);
                }
            } // ResultSet lukkes automatisk her
        } catch (SQLException e) {
            // Håndter databasefejl (f.eks. forbindelsesproblemer, SQL syntax fejl)
            System.err.println("DAO FEJL: SQLException ved søgning efter bruger '" + username + "'.");
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace(); // Vis fuld stack trace for debugging
            // user forbliver null
        } // Connection og PreparedStatement lukkes automatisk her

        return user; // Returner det fundne User objekt, eller null hvis ikke fundet/fejl
    }

    /**
     * Gemmer en ny bruger i databasen.
     * Finder først den korrekte RolleID baseret på brugerens UserRole enum.
     * Udfører handlingerne som en transaktion for at sikre dataintegritet.
     *
     * !! SIKKERHEDSADVARSEL: Denne metode gemmer kodeordet i KLARTEKST i databasen. !!
     * !! Implementer password HASHING (f.eks. med BCrypt) hurtigst muligt!       !!
     *
     * @param user Bruger-objektet der skal gemmes.
     * @return true hvis brugeren blev oprettet succesfuldt, ellers false.
     */
    public boolean saveUser(User user) {
        // !! VIGTIG ADVARSEL !!
        System.out.println("!!! ADVARSEL: UserDAO.saveUser gemmer KODEORD UDEN HASHING - MEGET USIKKERT !!!");

        String sqlFindRoleId = "SELECT RolleID FROM Brugerrolle WHERE RolleNavn = ?";
        String sqlInsertUser = "INSERT INTO Bruger (Brugernavn, Kodeord, RolleID) VALUES (?, ?, ?)";
        boolean success = false;
        Connection conn = null; // Deklarer uden for try-with-resources pga. transaktionsstyring

        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Start manuel transaktionsstyring

            int rolleId = -1; // Initialiser RolleID

            // Trin 1: Find RolleID for brugerens rolle
            System.out.println("DAO Save: Finder RolleID for RolleNavn: " + user.getRole().name());
            try (PreparedStatement pstmtFindRole = conn.prepareStatement(sqlFindRoleId)) {
                pstmtFindRole.setString(1, user.getRole().name()); // Brug Enum'ens navn (f.eks. "SUPERUSER")
                try (ResultSet rsRole = pstmtFindRole.executeQuery()) {
                    if (rsRole.next()) {
                        rolleId = rsRole.getInt("RolleID");
                        System.out.println("DAO Save: Fundet RolleID: " + rolleId);
                    } else {
                        // Hvis rollen ikke findes i Brugerrolle tabellen, kan brugeren ikke oprettes.
                        System.err.println("DAO FEJL: Kunne ikke finde RolleID for RolleNavn '" + user.getRole().name() + "' i Brugerrolle tabellen.");
                        conn.rollback(); // Rul transaktionen tilbage
                        return false; // Afslut med fejl
                    }
                }
            } // PreparedStatement og ResultSet for rolle-opslag lukkes her

            // Trin 2: Indsæt brugeren i Bruger tabellen med den fundne RolleID
            // !! IGEN: Klartekst kodeord - SKAL HASHES FØR DET GEMMES !!
            System.out.println("DAO Save: Indsætter bruger '" + user.getUsername() + "' med Kodeord (USIKKERT!) og RolleID " + rolleId);
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertUser)) {
                pstmtInsert.setString(1, user.getUsername());
                pstmtInsert.setString(2, user.getPassword()); // <---- GEMMER KLARTEKST KODEORD!
                pstmtInsert.setInt(3, rolleId);

                int affectedRows = pstmtInsert.executeUpdate(); // Udfør INSERT

                if (affectedRows > 0) {
                    conn.commit(); // Gennemfør transaktionen, da alt gik godt
                    System.out.println("DAO: Bruger oprettet succesfuldt i DB: " + user.getUsername());
                    success = true;
                } else {
                    // Dette burde normalt ikke ske, hvis der ikke kastes en exception, men for en sikkerheds skyld:
                    System.err.println("DAO FEJL: Bruger blev IKKE oprettet (affectedRows = 0), ruller transaktion tilbage.");
                    conn.rollback(); // Rul tilbage
                }
            } // PreparedStatement for insert lukkes her

        } catch (SQLException e) {
            // Fejl opstod under transaktionen
            System.err.println("DAO FEJL: SQLException under oprettelse af bruger '" + user.getUsername() + "'. Ruller transaktion tilbage.");
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            // Forsøg at rulle tilbage hvis muligt
            if (conn != null) {
                try {
                    System.err.println("DAO: Forsøger at rulle transaktion tilbage...");
                    conn.rollback();
                    System.err.println("DAO: Transaktion rullet tilbage.");
                } catch (SQLException ex) {
                    System.err.println("DAO FEJL: Kritisk fejl under forsøg på rollback: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            success = false; // Sørg for at returnere false ved fejl

        } finally {
            // Uanset succes eller fejl, sørg for at lukke forbindelsen og sætte autoCommit tilbage
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Sæt autoCommit tilbage til standard
                    conn.close(); // Luk forbindelsen
                    System.out.println("DAO: Databaseforbindelse lukket.");
                } catch (SQLException e) {
                    System.err.println("DAO FEJL: Fejl under lukning af databaseforbindelse.");
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    // Tilføj evt. metoder til at opdatere eller slette brugere senere
    // public boolean updateUser(User user) { ... }
    // public boolean deleteUser(int userId) { ... }

}