package it.petrinet.model.database;

import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComputationStepDAO implements DataAccessObject{

    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computationSteps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "computationId INTEGER NOT NULL, " +
                "netId TEXT NOT NULL, " + // netId qui si riferisce a petri_nets(netName)
                "transitionName TEXT, " +
                "markingState TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "FOREIGN KEY(computationId) REFERENCES computations(id) ON DELETE CASCADE, " + // Aggiunto ON DELETE CASCADE
                "FOREIGN KEY(netId) REFERENCES petri_nets(netName) ON DELETE CASCADE" +      // Aggiunto ON DELETE CASCADE
                ")";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            DatabaseManager.enableForeignKeys(statement);
            statement.execute(table);
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("createNotificationsTable", e);
        }
    }

    public static void deleteTable(){
        String command = "DROP TABLE IF EXISTS computationSteps;"; // Aggiunto IF EXISTS

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            DatabaseManager.enableForeignKeys(statement);
            statement.execute(command);
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("deleteNotificationsTable", e);
        }
    }

    public static void insertStep(ComputationStep step) {
        String command = "INSERT INTO computationSteps(computationId, netId, transitionName, markingState, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) { // Rimosso Statement non usato
            DatabaseManager.enableForeignKeys(statement); // Helper per PRAGMA
            p_statement.setInt(1, step.getComputationId());
            p_statement.setString(2, step.getNetId());
            p_statement.setString(3, step.getTransitionName());
            p_statement.setString(4, step.getTempMarkingState());
            p_statement.setLong(5, step.getTimestamp());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("insertStep", e);
        }
    }

    public static void removeStep(ComputationStep step) {
        String command = "DELETE FROM computationSteps WHERE id = ? AND computationId = ? AND netId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement); // Helper per PRAGMA

            p_statement.setLong(1, step.getId());
            p_statement.setInt(2, step.getComputationId());
            p_statement.setString(3, step.getNetId());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("removeStep", e);
        }
    }

    public static void removeAllStepsByComputation(Computation computation) {
        String command = "DELETE FROM computationSteps WHERE computationId = ? AND netId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setInt(1, ComputationsDAO.getIdByComputation(computation));
            p_statement.setString(2, computation.getNetId());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("removeAllStepsByComputation", e);
        }
    }

    public static void resetComputationSteps(Computation computation) {
        String command = "DELETE FROM computationSteps WHERE computationId = ? AND netId = ? " +
                "AND timestamp > (SELECT MIN(timestamp) FROM computationSteps WHERE computationId = ? AND netId = ?);";
    

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            int computationId = ComputationsDAO.getIdByComputation(computation);
            p_statement.setInt(1, computationId);
            p_statement.setString(2, computation.getNetId());
            p_statement.setInt(3, computationId);
            p_statement.setString(4, computation.getNetId());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("resetComputationSteps", e);
        }
    }

    public static ComputationStep getComputationStepByTimestamp(long timestamp)  {
        String command = "SELECT id, computationId, netId, transitionName, markingState, timestamp FROM computationSteps WHERE timestamp = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {
            DatabaseManager.enableForeignKeys(statement); // Helper per PRAGMA
            p_statement.setLong(1, timestamp);
            try (ResultSet result = p_statement.executeQuery()) {
                if (result.next()) {
                    return new ComputationStep(
                            result.getLong("id"),
                            result.getInt("computationId"),
                            result.getString("netId"),
                            result.getString("transitionName"),
                            result.getString("markingState"),
                            result.getLong("timestamp")
                    );
                }
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getComputationStepByTimestamp", e);
        }
        return null;
    }

    public static ComputationStep getLastComputationStep(Computation computation)  {
        int id = ComputationsDAO.getIdByComputation(computation);
        if (id < 0) {
            System.err.println("Computation given not in database.");
            return null;
        }
        String command = "SELECT id, computationId, netId, transitionName, markingState, timestamp " +
                "FROM computationSteps " +
                "WHERE computationId = ? " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1;";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setInt(1, id);

            try (ResultSet result = p_statement.executeQuery()) {
                if (result.next()) {
                    return new ComputationStep(
                            result.getLong("id"),
                            result.getInt("computationId"),
                            result.getString("netId"),
                            result.getString("transitionName"),
                            result.getString("markingState"),
                            result.getLong("timestamp")
                    );
                }
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getLastComputationStep", e);
        }
        return null;
    }

    public static List<ComputationStep> getStepsByComputation(Computation computation) {
        List<ComputationStep> wantedSteps = new ArrayList<>();

        String command = "SELECT id, computationId, netId, transitionName, markingState, timestamp " +
                "FROM computationSteps " +
                "WHERE computationId = ? AND netId = ? " +
                "ORDER BY timestamp ASC;";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setInt(1, ComputationsDAO.getIdByComputation(computation));
            p_statement.setString(2, computation.getNetId());

            try (ResultSet result = p_statement.executeQuery()) {
                while (result.next()) {
                    wantedSteps.add( new ComputationStep(
                            result.getLong("id"),
                            result.getInt("computationId"),
                            result.getString("netId"),
                            result.getString("transitionName"),
                            result.getString("markingState"),
                            result.getLong("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getStepsByComputation", e);
        }
        return wantedSteps;
    }

    public static Computation getComputationByStep(ComputationStep step) {
        String command = "SELECT computationId FROM computationSteps WHERE id = ? AND netId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setLong(1, step.getId());
            p_statement.setString(2, step.getNetId());
            try (ResultSet result = p_statement.executeQuery()) {
                if(result.next()){
                    return ComputationsDAO.getComputationById(result.getInt(1));
                }
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getComputationByStep", e);
        }
        return null;
    }

    /**
     * Retrieves the last (most recent by timestamp) ComputationStep for a given PetriNet.
     *
     * @param petriNetId The ID (netName) of the PetriNet.
     * @return The last ComputationStep for the specified PetriNet, or null if none is found.
     * @ If the provided petriNetId is null or empty.
     */
    public static ComputationStep getLastComputationStepForPetriNet(String petriNetId)  {
        ComputationStep lastStep = null;
        String query = "SELECT id, computationId, netId, transitionName, markingState, timestamp " +
                "FROM computationSteps " +
                "WHERE netId = ? " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1;";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, petriNetId);

            try (ResultSet result = p_statement.executeQuery()) {
                if (result.next()) {
                    lastStep = new ComputationStep(
                            result.getLong("id"),
                            result.getInt("computationId"),
                            result.getString("netId"),
                            result.getString("transitionName"),
                            result.getString("markingState"),
                            result.getLong("timestamp")
                    );
                }
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getLastComputationStepForPetriNet", e);
        }
        return lastStep;
    }
}