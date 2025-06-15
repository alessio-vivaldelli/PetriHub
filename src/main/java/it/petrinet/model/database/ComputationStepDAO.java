package it.petrinet.model.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ComputationStepDAO {
    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computationSteps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "computationId INTEGER NOT NULL, " +
                "netId INTEGER NOT NULL, " +
                "transition TEXT, " +
                "markingLocation TEXT NOT NULL)";

        try (Connection connection = DatabaseManager.getComputationsDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
