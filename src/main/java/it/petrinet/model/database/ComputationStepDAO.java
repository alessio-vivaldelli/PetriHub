package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComputationStepDAO implements DataAccessObject{
    public static void main(String[] args) throws InputTypeException {
        insertStep(new ComputationStep(5, 2, "net3", "T4", "p2:1,p3:1", 1686302400L));
        insertStep(new ComputationStep(5, 3, "net10", "T4", "p2:1,p3:1", 1702504800L));
    }

    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computationSteps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "computationId INTEGER NOT NULL, " +
                "netId TEXT NOT NULL, " +
                "transitionName TEXT, " +
                "markingState TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "FOREIGN KEY(computationId) REFERENCES computations(id), " +
                "FOREIGN KEY(netId) REFERENCES petri_nets(netName)" +
                ")";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void deleteTable(){
        String command = "DROP TABLE computationSteps;";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");statement.execute(command);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertStep(Object step) throws InputTypeException{
        String command = "INSERT INTO computationSteps(computationId, netId, transitionName, markingState, timestamp) VALUES (?, ?, ?, ?, ?)";
        try {
            if (step instanceof ComputationStep s) {

                try{
                    if (!DatabaseManager.tableExists("steps")) {
                        ComputationStepDAO dao = new ComputationStepDAO();
                        dao.createTable();
                    }
                }catch(SQLException e){
                    e.printStackTrace();
                }

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setInt(1, s.getComputationId());
                    p_statement.setString(2, s.getNetId());
                    p_statement.setString(3, s.getTransitionName());
                    p_statement.setString(4, s.getTempMarkingState());
                    p_statement.setLong(5, s.getTimestamp());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }
    public static void removeStep(Object step) throws InputTypeException{
        String command = "DELETE FROM computationSteps WHERE id = ? AND computationId = ? AND netId = ?";
        try {
            if (step instanceof ComputationStep s) {

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setLong(1, s.getId());
                    p_statement.setInt(2, s.getComputationId());
                    p_statement.setString(3, s.getNetId());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static ComputationStep getComputationStepByTimestamp(Object timestamp)throws InputTypeException{
        try{
            if(timestamp instanceof Long time){
                String command = "SELECT * FROM computations WHERE timestamp = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setLong(1, time);
                    ResultSet result = p_Statement.executeQuery();

                    if(result.next()){
                        new ComputationStep(
                                result.getLong(1),
                                result.getInt(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5),
                                result.getLong(6)
                        );
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return null;
    }

    public static ComputationStep getLastComputationStep(Object computation)throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                int id = ComputationsDAO.getIdByComputation(c);
                if(id < 0){
                    System.err.println("The computation given is not in the database");
                    return null;
                }
                String command2 = "SELECT * " +
                        "FROM computationSteps " +
                        "WHERE computationId = ? " +
                        "AND timestamp = (SELECT MAX(timestamp) FROM computationSteps WHERE ComputationId = ?);";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command2)) {
                    p_Statement.setLong(1, id);
                    p_Statement.setLong(2,id);
                    ResultSet result = p_Statement.executeQuery();

                    if(result.next()) {
                        return new ComputationStep(
                                result.getLong(1),
                                result.getInt(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5),
                                result.getLong(6)
                        );
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return null;
    }

    public static List<ComputationStep> getStepsByComputation(Object computation) throws InputTypeException{
        List<ComputationStep> wantedSteps = new ArrayList<ComputationStep>();
        try{
            if(computation instanceof Computation c){
                String command = "SELECT * FROM computationSteps WHERE userId = ? AND creatorId = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, c.getUserId());
                    p_statement.setString(1, c.getCreatorId());
                    ResultSet result = p_statement.executeQuery();

                    while (result.next()) {
                        wantedSteps.add( new ComputationStep(
                                result.getLong(1),
                                result.getInt(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5),
                                result.getLong(6)
                        ));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedSteps;
    }

    public static Computation getComputationByStep(Object step) throws InputTypeException{
        String command = "SELECT computationId FROM computationSteps WHERE id = ? AND netId = ?";
        try {
            if (step instanceof ComputationStep s) {

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setLong(1, s.getId());
                    p_statement.setString(3, s.getNetId());
                    ResultSet result = p_statement.executeQuery();
                    if(result.next()){
                        return ComputationsDAO.getComputationById(result.getInt(1));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION_STEP);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }

        return null;
    }

}
