package it.petrinet.model.database;

import it.petrinet.model.ComputationStep;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ComputationStepDAO {
    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computationSteps (" +
                "id INTEGER AUTOINCREMENT, " +
                "computationId INTEGER NOT NULL, " +
                "netId INTEGER NOT NULL, " +
                "transition TEXT, " +
                "markingLocation TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL)";


        try (Connection connection = DatabaseManager.getComputationsDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertStep(){

    }

    public static void deleteStep(){

    }

    public static ComputationStep getComputationBy(){
        return null;
    }

}
