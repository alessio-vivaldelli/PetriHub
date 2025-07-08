package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComputationStepDAO implements DataAccessObject{

    private static final String typeErrorMessage = "Invalid input type provided."; // Definizione del messaggio di errore

    public static void main(String[] args) throws InputTypeException {
        ComputationStepDAO dao = new ComputationStepDAO();
        dao.createTable();
//        insertStep(new ComputationStep(5, 2, "net3", "T4", "p2:1,p3:1", 1686302400L));
//        insertStep(new ComputationStep(5, 3, "net10", "T4", "p2:1,p3:1", 1702504800L));
    }

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
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.execute(table);
        } catch (SQLException ex) {
            System.err.println("Error creating computationSteps table: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void deleteTable(){
        String command = "DROP TABLE IF EXISTS computationSteps;"; // Aggiunto IF EXISTS

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.execute(command);
        } catch (SQLException ex) {
            System.err.println("Error deleting computationSteps table: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void insertStep(Object step) throws InputTypeException{
        String command = "INSERT INTO computationSteps(computationId, netId, transitionName, markingState, timestamp) VALUES (?, ?, ?, ?, ?)";
        if (!(step instanceof ComputationStep s)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        try {
            // Nota: La creazione della tabella dovrebbe essere gestita altrove, non per ogni insert.
            // try{
            //     if (!DatabaseManager.tableExists("computationSteps")) { // Corretto il nome della tabella
            //         ComputationStepDAO dao = new ComputationStepDAO();
            //         dao.createTable();
            //     }
            // }catch(SQLException e){
            //     e.printStackTrace();
            // }

            try (Connection connection = DatabaseManager.getDBConnection();
                 PreparedStatement p_statement = connection.prepareStatement(command)) { // Rimosso Statement non usato
                setupForeignKeys(connection); // Helper per PRAGMA
                p_statement.setInt(1, s.getComputationId());
                p_statement.setString(2, s.getNetId());
                p_statement.setString(3, s.getTransitionName());
                p_statement.setString(4, s.getTempMarkingState());
                p_statement.setLong(5, s.getTimestamp());
                p_statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error inserting ComputationStep: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeStep(Object step) throws InputTypeException{
        String command = "DELETE FROM computationSteps WHERE id = ? AND computationId = ? AND netId = ?";
        if (!(step instanceof ComputationStep s)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) { // Rimosso Statement non usato
            setupForeignKeys(connection); // Helper per PRAGMA
            p_statement.setLong(1, s.getId());
            p_statement.setInt(2, s.getComputationId());
            p_statement.setString(3, s.getNetId());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing ComputationStep: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeAllStepsByComputation(Object computation) throws InputTypeException{
        String command = "DELETE FROM computationSteps WHERE computationId = ? AND netId = ?";
        if (!(computation instanceof Computation c)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) { // Rimosso Statement non usato
            setupForeignKeys(connection); // Helper per PRAGMA
            // Assumo che getIdByComputation non lanci eccezioni e ritorni un ID valido o gestisca internamente errori
            p_statement.setInt(1, ComputationsDAO.getIdByComputation(c));
            p_statement.setString(2, c.getNetId());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing all ComputationSteps by computation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void resetComputationSteps(Object computation) throws InputTypeException{
        String command = "DELETE FROM computationSteps WHERE computationId = ? AND netId = ? " +
                "AND timestamp > (SELECT MIN(timestamp) FROM computationSteps WHERE computationId = ? AND netId = ?);";
        if (!(computation instanceof Computation c)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) { // Rimosso Statement non usato
            setupForeignKeys(connection); // Helper per PRAGMA
            int computationId = ComputationsDAO.getIdByComputation(c);
            p_statement.setInt(1, computationId);
            p_statement.setString(2, c.getNetId());
            p_statement.setInt(3, computationId);
            p_statement.setString(4, c.getNetId());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resetting computation steps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ComputationStep getComputationStepByTimestamp(Object timestamp) throws InputTypeException {
        if (!(timestamp instanceof Long time)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        String command = "SELECT id, computationId, netId, transitionName, markingState, timestamp FROM computationSteps WHERE timestamp = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            setupForeignKeys(connection); // Helper per PRAGMA
            p_statement.setLong(1, time);
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
        } catch (SQLException ex) {
            System.err.println("Error getting ComputationStep by timestamp: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public static ComputationStep getLastComputationStep(Object computation) throws InputTypeException {
        if (!(computation instanceof Computation c)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        int id = ComputationsDAO.getIdByComputation(c);
        if (id < 0) {
            System.err.println("The computation given is not in the database.");
            return null;
        }

        String command = "SELECT id, computationId, netId, transitionName, markingState, timestamp " +
                "FROM computationSteps " +
                "WHERE computationId = ? " +
                "ORDER BY timestamp DESC " + // Ordina per timestamp discendente
                "LIMIT 1;"; // Prendi solo il primo risultato (il più recente)

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            setupForeignKeys(connection); // Helper per PRAGMA
            p_statement.setInt(1, id); // Usa setInt perché l'ID della computazione è un INT

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
        } catch (SQLException ex) {
            System.err.println("Error getting last ComputationStep for computation: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public static List<ComputationStep> getStepsByComputation(Object computation) throws InputTypeException{
        List<ComputationStep> wantedSteps = new ArrayList<>();
        if (!(computation instanceof Computation c)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        String command = "SELECT id, computationId, netId, transitionName, markingState, timestamp " +
                "FROM computationSteps " +
                "WHERE computationId = ? AND netId = ? " + // Corretto i parametri
                "ORDER BY timestamp ASC;"; // Ordina in ordine crescente per visualizzare la storia

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) { // Rimosso Statement non usato
            setupForeignKeys(connection); // Helper per PRAGMA
            p_statement.setInt(1, ComputationsDAO.getIdByComputation(c)); // get id of computation
            p_statement.setString(2, c.getNetId());
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
        } catch (SQLException ex) {
            System.err.println("Error getting steps by computation: " + ex.getMessage());
            ex.printStackTrace();
        }
        return wantedSteps;
    }

    public static Computation getComputationByStep(Object step) throws InputTypeException{
        String command = "SELECT computationId FROM computationSteps WHERE id = ? AND netId = ?";
        if (!(step instanceof ComputationStep s)) {
            throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
        }

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) { // Rimosso Statement non usato
            setupForeignKeys(connection); // Helper per PRAGMA
            p_statement.setLong(1, s.getId());
            p_statement.setString(2, s.getNetId()); // Corretto l'indice del parametro
            try (ResultSet result = p_statement.executeQuery()) {
                if(result.next()){
                    return ComputationsDAO.getComputationById(result.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting Computation by step: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the last (most recent by timestamp) ComputationStep for a given PetriNet.
     *
     * @param petriNetId The ID (netName) of the PetriNet.
     * @return The last ComputationStep for the specified PetriNet, or null if none is found.
     * @throws InputTypeException If the provided petriNetId is null or empty.
     */
    public static ComputationStep getLastComputationStepForPetriNet(String petriNetId) throws InputTypeException {
        if (petriNetId == null || petriNetId.trim().isEmpty()) {
            throw new InputTypeException("PetriNet ID cannot be null or empty.", InputTypeException.ExceptionType.PETRI_NET);
        }

        ComputationStep lastStep = null;
        String query = "SELECT id, computationId, netId, transitionName, markingState, timestamp " +
                "FROM computationSteps " +
                "WHERE netId = ? " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1;";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(query)) {
            setupForeignKeys(connection); // Abilita le foreign keys
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
        } catch (SQLException ex) {
            System.err.println("Error retrieving last ComputationStep for PetriNet '" + petriNetId + "': " + ex.getMessage());
            ex.printStackTrace();
            // Puoi scegliere di rilanciare l'eccezione o gestirla in modo diverso qui
        }
        return lastStep;
    }

    /**
     * Helper method to enable foreign keys for SQLite.
     * This should ideally be handled once per connection or globally.
     */
    private static void setupForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
    }
}