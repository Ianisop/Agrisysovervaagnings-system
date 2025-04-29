package dk.agrisys.pigfeedingsystem.dao; // Eller org.example.dao hvis du ikke har refaktoreret

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Håndterer databaseforbindelsen ved at læse konfiguration fra db.properties.
 * Sikrer at driveren er indlæst og giver en metode til at hente en forbindelse.
 *
 * (Hvem har skrevet: [Dit Navn/Gruppens Navn])
 */
public class DatabaseConnector {

    // Properties objekt til at holde konfigurationen fra filen
    private static final Properties dbProperties = new Properties();

    // Statiske variable til at holde de indlæste forbindelsesoplysninger
    private static String dbUrl = null;
    private static String dbUser = null;
    private static String dbPassword = null;

    // Statisk initialiseringsblok: Kører én gang når klassen indlæses.
    // Indlæser properties-filen og JDBC-driveren.
    static {
        System.out.println("Initializing DatabaseConnector...");

        // Trin 1: Indlæs databasekonfiguration fra db.properties
        // Bruger try-with-resources for at sikre, at InputStream lukkes automatisk.
        try (InputStream input = DatabaseConnector.class.getClassLoader().getResourceAsStream("db.properties")) {

            if (input == null) {
                // Kritisk fejl: Konfigurationsfilen kunne ikke findes i classpath'en (resources mappen).
                System.err.println("FATAL ERROR: Unable to find 'db.properties' file in resources folder.");
                System.err.println("Please ensure 'db.properties' exists in 'src/main/resources'.");
                // Kaster en RuntimeException for at stoppe applikationsstart, da DB-forbindelse er essentiel.
                throw new RuntimeException("db.properties not found in classpath");
            }

            // Indlæs egenskaberne fra filen ind i Properties objektet.
            dbProperties.load(input);

            // Hent de specifikke værdier fra properties objektet.
            dbUrl = dbProperties.getProperty("db.url");
            dbUser = dbProperties.getProperty("db.user");
            dbPassword = dbProperties.getProperty("db.password");

            // Validering: Tjek om alle nødvendige properties blev fundet i filen.
            if (dbUrl == null || dbUrl.trim().isEmpty() ||
                    dbUser == null || dbUser.trim().isEmpty() || // Tillad evt. tomt password hvis nødvendigt for DB setup
                    dbPassword == null ) {
                System.err.println("FATAL ERROR: Missing required properties (db.url, db.user, db.password) in db.properties.");
                throw new RuntimeException("Missing required properties in db.properties");
            }

            System.out.println("Database properties loaded successfully from db.properties.");
            // Undgå at printe password i logs:
            System.out.println("   DB URL (partial): " + dbUrl.split(";")[0]); // Viser kun server/port/db-navn del
            System.out.println("   DB User: " + dbUser);


        } catch (IOException ex) {
            // Fejl under læsning af filen (f.eks. I/O fejl).
            System.err.println("FATAL ERROR: IOException while loading db.properties.");
            ex.printStackTrace(); // Vis stack trace for debugging.
            throw new RuntimeException("Failed to load db.properties", ex);
        }

        // Trin 2: Indlæs MSSQL JDBC Driveren
        try {
            // Forsøger at indlæse driverklassen. Dette registrerer driveren hos DriverManager.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("MSSQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            // Kritisk fejl: Driverklassen blev ikke fundet. Skyldes typisk manglende Maven dependency.
            System.err.println("FATAL ERROR: MSSQL JDBC Driver not found!");
            System.err.println("Please ensure the 'mssql-jdbc' dependency is correctly added in your pom.xml.");
            e.printStackTrace(); // Vis stack trace for debugging.
            throw new RuntimeException("MSSQL Driver class not found!", e);
        }

        System.out.println("DatabaseConnector initialization complete.");
    }

    /**
     * Opretter og returnerer en ny forbindelse til databasen.
     * Bruger de forbindelsesoplysninger, der blev indlæst fra db.properties.
     *
     * @return En aktiv Connection til databasen.
     * @throws SQLException Hvis der opstår en fejl under forsøget på at oprette forbindelse
     *                      (f.eks. forkert URL, bruger/password, netværksproblem, server nede).
     */
    public static Connection getConnection() throws SQLException {
        // Tjek om initialiseringen fejlede (selvom RuntimeException burde have stoppet det)
        if (dbUrl == null || dbUser == null || dbPassword == null) {
            throw new SQLException("Database Connector not properly initialized. Check logs for errors during startup.");
        }

        System.out.println("Attempting to establish database connection...");
        // Opretter forbindelsen vha. DriverManager og de indlæste credentials.
        Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        // Hvis vi når hertil uden exception, er forbindelsen oprettet.
        System.out.println("Database connection established successfully.");
        return connection;
    }

    /**
     * En simpel metode til at teste, om en databaseforbindelse kan oprettes.
     * Nyttig at kalde tidligt i applikationens opstart for at verificere konfigurationen.
     * Lukker automatisk forbindelsen efter test via try-with-resources.
     */
    public static void testConnection() {
        System.out.println("Performing database connection test...");
        try (Connection conn = getConnection()) {
            // Hvis getConnection() ikke kaster en SQLException, er forbindelsen OK.
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: Database connection test was successful!");
            } else {
                // Dette burde teknisk set ikke ske, hvis getConnection virker korrekt.
                System.err.println("WARNING: Database connection test completed, but connection is null or closed unexpectedly.");
            }
        } catch (SQLException e) {
            // Fejl under oprettelse af forbindelse. Viser en mere brugervenlig fejl.
            System.err.println("FAILURE: Database connection test failed!");
            System.err.println("   Error message: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   Check:");
            System.err.println("     - Database server is running and accessible.");
            System.err.println("     - Network connection/firewall rules.");
            System.err.println("     - Credentials (URL, user, password) in 'db.properties' are correct.");
            System.err.println("     - Database name exists.");
            // Vis fuld stack trace for detaljeret debugging
            // e.printStackTrace();
        } catch (RuntimeException e) {
            // Fanger fejl fra den statiske initialiseringsblok (f.eks. manglende properties fil/driver)
            System.err.println("FAILURE: Database connection test failed due to initialization error:");
            System.err.println("   Error message: " + e.getMessage());
            // Den underliggende årsag (f.eks. ClassNotFoundException) vil være i loggen fra static blokken.
        }
    }
}