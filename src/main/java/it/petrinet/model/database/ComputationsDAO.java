package it.petrinet.model.database;

import it.petrinet.exceptions.ExceptionType;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComputationsDAO implements DataAccessObject{

    public static void main(String args[]) throws InputTypeException {
        Computation c = new Computation("piero", "davide", "ddd", 2);
        insertComputation(c);
    }

    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "netId TEXT NOT NULL, " +
                "creatorId TEXT NOT NULL, " +
                "userId TEXT NOT NULL," +
                "startDate INTEGER NOT NULL, " +
                "endDate INTEGER," +
                "UNIQUE (netId, userId, creatorId))";

        try (Connection connection = DatabaseManager.getComputationsDBConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertComputation(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                String command = "INSERT INTO computations(netId, creatorId, userId, startDate, endDate) VALUES (?, ?, ?, ?, ?)";

                if(!DatabaseManager.tableExists("computations", "computations")){
                    ComputationsDAO dao = new ComputationsDAO();
                    dao.createTable();
                }
                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, c.getNetId());
                    p_Statement.setString(2, c.getCreatorId());
                    p_Statement.setString(3, c.getUserId());
                    p_Statement.setInt(4, c.getStartDate());
                    if(c.getEndDate()!= -1){
                        p_Statement.setInt(5, c.getEndDate());
                    }
                    p_Statement.executeUpdate();
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static Computation getComputationById(Object id)throws InputTypeException{
        try{
            if(id instanceof Integer i){
                String command = "SELECT * FROM computations WHERE id = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setInt(1, i);
                    ResultSet result = p_Statement.executeQuery();

                    if(result.next()){
                        return new Computation(
                            result.getString(1),
                            result.getString(2),
                            result.getString(3),
                            result.getInt(4),
                            result.getInt(5)
                        );
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return null;
    }

    public static void deleteComputationById(Object id) throws InputTypeException{
        try{
            if(id instanceof Integer i){
                String command = "DELETE FROM computations WHERE id = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setInt(1, i);
                    p_Statement.executeUpdate();
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static void deleteComputation(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                String command = "DELETE FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, c.getNetId());
                    p_Statement.setString(2, c.getCreatorId());
                    p_Statement.setString(3, c.getUserId());
                    p_Statement.executeUpdate();
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static boolean isComplete(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation com){
                String command = "SELECT endDate FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, com.getNetId());
                    p_Statement.setString(2, com.getCreatorId());
                    p_Statement.setString(3, com.getUserId());
                    ResultSet result = p_Statement.executeQuery();

                    if(result.next()){
                        result.getInt(1);
                        return !result.wasNull();   //se è not null, c'è una data inserita, quindi ritorna vero
                    }

                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return false;
    }

    public static void setAsCompleted(Object computation, Object endDate) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                if(endDate instanceof Integer date){
                    String command = "UPDATE computations SET endDate = ? WHERE netId = ? AND userId = ? AND creatorId = ?";

                    try (Connection connection = DatabaseManager.getComputationsDBConnection();
                         PreparedStatement p_Statement = connection.prepareStatement(command)){
                        p_Statement.setInt(1, date);
                        p_Statement.setString(2, c.getNetId());
                        p_Statement.setString(3, c.getUserId());
                        p_Statement.setString(4, c.getCreatorId());
                        p_Statement.executeUpdate();
                    }
                    catch(SQLException ex){
                        ex.printStackTrace();
                    }
                }
                else{
                    throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static List<Computation> getComputationsByNet(Object petriNet) throws InputTypeException{
        List<Computation> wantedComputations = new ArrayList<Computation>();
        try{
            if(petriNet instanceof PetriNet net){
                String command = "SELECT * FROM computations WHERE netId = ? AND creatorId = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, net.getNetName());
                    p_Statement.setString(2, net.getCreatorId());
                    ResultSet result = p_Statement.executeQuery();

                    while (result.next()) {
                        wantedComputations.add(new Computation(
                                result.getString(1),
                                result.getString(2),
                                result.getString(3),
                                result.getInt(4),
                                result.getInt(5)
                        ));
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedComputations;
    }

    public static int getIdByComputation(Object computation) throws InputTypeException{
        try {
            if (computation instanceof Computation c) {
                String command = "SELECT id FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)) {
                    p_Statement.setString(1, c.getNetId());
                    p_Statement.setString(2, c.getCreatorId());
                    p_Statement.setString(3,c.getUserId());
                    ResultSet result = p_Statement.executeQuery();

                    if(result.next()){
                        return result.getInt(1);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
                e.ErrorPrinter();
        }
        return -1;
    }

    public static List<Computation> getComputationsByUser(Object user) throws InputTypeException{
        List<Computation> wantedComputations = new ArrayList<Computation>();
        try{
            if(user instanceof User u){
                String command = "SELECT * FROM computations WHERE creatorId = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, u.getUsername());
                    ResultSet result = p_Statement.executeQuery();
                    while(result.next()){
                        wantedComputations.add( new Computation(
                                result.getString(1),
                                result.getString(2),
                                result.getString(3),
                                result.getInt(4),
                                result.getInt(5)
                        ));
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }

        return wantedComputations;
    }

    public static List<Computation> getComputationsByAdmin(Object user) throws InputTypeException{
        List<Computation> wantedComputations = new ArrayList<Computation>();
        try{
            if(user instanceof User u){
                String command = "SELECT * FROM computations WHERE Id = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, u.getUsername());
                    ResultSet result = p_Statement.executeQuery();
                    while(result.next()){
                        wantedComputations.add( new Computation(
                                result.getString(1),
                                result.getString(2),
                                result.getString(3),
                                result.getInt(4),
                                result.getInt(5)
                        ));
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedComputations;
    }

    public static List<Computation> getComputationsByAdminAndUser(Object user, Object admin) throws InputTypeException{
        List<Computation> wantedComputations = new ArrayList<Computation>();
        try{
            if(user instanceof User u){
                if(admin instanceof User a) {
                    String command = "SELECT * FROM computations WHERE userId = ? AND creatorId = ?";

                    try (Connection connection = DatabaseManager.getComputationsDBConnection();
                         PreparedStatement p_Statement = connection.prepareStatement(command)) {
                        p_Statement.setString(1, u.getUsername());
                        p_Statement.setString(1, a.getUsername());
                        ResultSet result = p_Statement.executeQuery();

                        while (result.next()) {
                            wantedComputations.add( new Computation(
                                    result.getString(1),
                                    result.getString(2),
                                    result.getString(3),
                                    result.getInt(4),
                                    result.getInt(5)
                            ));
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                else{
                    throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedComputations;
    }

    public static List<User> getSubscribedUsersByNet(Object p_net){
        List<User> subscribedUsers = new ArrayList<User>();
        try{
            if(p_net instanceof PetriNet net){
                String command = "SELECT userId FROM computations WHERE netId = ?";

                try (Connection connection = DatabaseManager.getComputationsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, net.getNetName());
                    ResultSet result = p_Statement.executeQuery();
                    while(result.next()){
                        subscribedUsers.add(UserDAO.getUserByUsername(result.getString(1))
                        );
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return subscribedUsers;
    }
}
