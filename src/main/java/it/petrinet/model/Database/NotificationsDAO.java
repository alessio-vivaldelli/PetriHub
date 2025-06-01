package it.petrinet.model.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class NotificationsDAO {

    //private final String dbURL = "jdbc:sqlite:/home/davide/IdeaProjects/PetriNetProject2025/notifications.db";
    //private final String dbURL = "jdbc:sqlite:notifications.db";

    public static void main(String[] args) {
        final String dbURL = "jdbc:sqlite:/home/davide/IdeaProjects/PetriNetProject2025/notifications.db";
        String sql = "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT);";

        try {
            Class.forName("org.sqlite.JDBC"); // Carica il driver
            Connection conn = DriverManager.getConnection(dbURL);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println("Tabella creata con successo");
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC non trovato:");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Errore SQL:");
            e.printStackTrace();
        }
    }
}