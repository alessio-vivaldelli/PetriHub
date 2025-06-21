package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComputationStepDAO implements DataAccessObject{
    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computationSteps (" +
                "id INTEGER AUTOINCREMENT PRIMARY KEY, " +
                "computationId INTEGER NOT NULL, " +
                "netId TEXT NOT NULL, " +
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

    public static void insertStep(Object step) throws InputTypeException{
        String command = "INSERT INTO computationSteps(computationId, netId, transition, markingLocation, timestamp) VALUES (?, ?, ?, ?, ?)";
        try {
            if (step instanceof ComputationStep s) {
                if (!DatabaseManager.tableExists("computations", "computationSteps")) {
                    ComputationStepDAO dao = new ComputationStepDAO();
                    dao.createTable();
                }

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setInt(1, s.getId());
                    p_statement.setInt(2, s.getComputationId());
                    p_statement.setString(3, s.getNetId());
                    p_statement.setString(4, s.getTransition());
                    p_statement.setString(5, s.getMarkingLocation());
                    p_statement.setInt(6, s.getTimestamp());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static void deleteStep(Object step) throws InputTypeException{
        String command = "DELETE FROM computationSteps WHERE id = ? AND computationId = ? AND netId = ?";
        try {
            if (step instanceof ComputationStep s) {

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setInt(1, s.getId());
                    p_statement.setInt(2, s.getComputationId());
                    p_statement.setString(3, s.getNetId());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static ComputationStep getComputationStepByTimestamp(Object timestamp)throws InputTypeException{
        try{
            if(timestamp instanceof String time){
                String command = "SELECT * FROM computations WHERE timestamp = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, time);
                    ResultSet result = p_Statement.executeQuery();

                    if(result.next()){
                        new ComputationStep(
                                result.getInt(1),
                                result.getInt(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5),
                                result.getInt(6)
                        );
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
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

                String command2 = "SELECT * FROM computationSteps WHERE Computation = ? and MAX(timestamp) = timestamp";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command2)) {
                    p_Statement.setInt(1, id);
                    ResultSet result = p_Statement.executeQuery();

                    if(result.next()) {
                        return new ComputationStep(
                                result.getInt(1),
                                result.getInt(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5),
                                result.getInt(6)
                        );
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
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

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)) {
                    p_Statement.setString(1, c.getUserId());
                    p_Statement.setString(1, c.getCreatorId());
                    ResultSet result = p_Statement.executeQuery();

                    while (result.next()) {
                        wantedSteps.add( new ComputationStep(
                                result.getInt(1),
                                result.getInt(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5),
                                result.getInt(6)
                        ));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
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

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setInt(1, s.getId());
                    p_statement.setString(3, s.getNetId());
                    ResultSet result = p_statement.executeQuery();
                    if(result.next()){
                        return ComputationsDAO.getComputationById(result.getInt(1));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }

        return null;
    }

}
