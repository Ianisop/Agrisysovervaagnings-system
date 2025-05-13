package dk.agrisys.pigfeedingsystem.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class DatabaseConnector {

    // --- Database forbindelsesoplysninger (SQL SERVER EKSEMPEL) ---
    // SKAL hentes fra en sikker konfiguration!
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Agrisys;encrypt=true;trustServerCertificate=true;"; // Erstat servernavn!
    private static final String DB_USER = "admin"; // Erstat!
    private static final String DB_PASSWORD = "1234"; // Erstat!



    // Placeholder - returnerer null indtil videre for at undgå fejl under kørsel uden DB
    public static Connection getConnection() throws SQLException {
        System.out.println("Forsøger at forbinde til databasen (PLACEHOLDER - returnerer null)");
        // Udkommenter linjen nedenfor NÅR du har konfigureret din MSSQL database

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // <-- FJERN DENNE LINJE og udkommenter ovenstående NÅR DB er klar
    }

    // Metode til at teste forbindelsen (kald den f.eks. i App.java for at se om driveren virker)
    public static void testConnection() {
        Connection conn = null;
        try {
            // Sørg for at MSSQL JDBC driveren er i din pom.xml
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // Nødvendig for ældre JDBC versioner / setups
            System.out.println("MSSQL Driver fundet.");
            conn = getConnection(); // Prøv at oprette forbindelse
            if (conn != null) {
            System.out.println("Databaseforbindelse OK!");
            conn.close();
             } else {
                 System.out.println("Databaseforbindelse fejlede (getConnection returnerede null).");
             }
            System.out.println("Database test afsluttet (uden rigtig forbindelse).");

        } catch (ClassNotFoundException e) {
            System.err.println("MSSQL JDBC Driver blev IKKE fundet! Tilføj dependency til pom.xml.");
            e.printStackTrace();
             } catch (SQLException e) {
                System.err.println("Kunne ikke forbinde til databasen: " + e.getMessage());
                e.printStackTrace();
        } finally {
             if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}