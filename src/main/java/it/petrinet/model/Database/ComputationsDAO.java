package it.petrinet.model.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ComputationsDAO implements DataAccessObject{
    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "netId INTEGER NOT NULL, " +
                "creatorId TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL, " +
                "isComplete BOOLEAN NOT NULL)";

        try (Connection connection = DatabaseManager.getUserDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
