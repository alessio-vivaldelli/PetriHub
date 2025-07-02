package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComputationsDAO implements DataAccessObject{
    public static void main(String[] args) throws InputTypeException {
        insertComputation(new Computation("net2","ale","davide", 34));
        insertComputation(new Computation("net3","ale","Davide", 34));
        insertComputation(new Computation("net10","Davide","Antonio", 34));
        System.out.println(getNetsSubscribedWithTimestampByUser(new User("Davide", "pass", true)));

    }

    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "netId TEXT NOT NULL, " +
                "creatorId TEXT NOT NULL, " +
                "userId TEXT NOT NULL," +
                "startDate LONGINT, " +
                "endDate LONGINT, " +
                "UNIQUE (netId, userId, creatorId), " +
                "FOREIGN KEY (netId) REFERENCES petri_nets(netName), " +
                "FOREIGN KEY (creatorId) REFERENCES users(username), " +
                "FOREIGN KEY (userId) REFERENCES users(username))";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteTable(){
        String command = "DROP TABLE computations;";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.execute(command);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertComputation(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                String command = "INSERT INTO computations(netId, creatorId, userId, startDate, endDate) VALUES (?, ?, ?, ?, ?)";

                try{
                    if(!DatabaseManager.tableExists("computations")){
                        ComputationsDAO dao = new ComputationsDAO();
                        dao.createTable();
                    }
                }
                catch(SQLException e){
                    e.printStackTrace();
                }

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, c.getNetId());
                    p_statement.setString(2, c.getCreatorId());
                    p_statement.setString(3, c.getUserId());
                    if(c.getStartTimestamp()>0){
                        p_statement.setLong(4, c.getStartTimestamp());
                    }
                    if(c.getEndTimestamp()>0){
                        p_statement.setLong(5, c.getEndTimestamp());
                    }
                    p_statement.executeUpdate();
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
    }

    public static Computation getComputationById(Object id)throws InputTypeException{
        try{
            if(id instanceof Integer i){
                String command = "SELECT * FROM computations WHERE id = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setInt(1, i);
                    ResultSet result = p_statement.executeQuery();

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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return null;
    }

    public static void removeComputationById(Object id) throws InputTypeException{
        try{
            if(id instanceof Integer i){
                String command = "DELETE FROM computations WHERE id = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setInt(1, i);
                    p_statement.executeUpdate();
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
    }

    public static void removeComputation(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                String command = "DELETE FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, c.getNetId());
                    p_statement.setString(2, c.getCreatorId());
                    p_statement.setString(3, c.getUserId());
                    p_statement.executeUpdate();
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
    }

    public static boolean isComplete(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation com){
                String command = "SELECT endDate FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, com.getNetId());
                    p_statement.setString(2, com.getCreatorId());
                    p_statement.setString(3, com.getUserId());
                    ResultSet result = p_statement.executeQuery();

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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return false;
    }

    public static void setAsStarted(Object computation, Object startDate) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                if(startDate instanceof Integer date){
                    String command = "UPDATE computations SET startDate = ? WHERE netId = ? AND userId = ? AND creatorId = ?";

                    try (Connection connection = DatabaseManager.getDBConnection();
                         PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                         p_statement.setInt(1, date);
                        p_statement.setString(2, c.getNetId());
                        p_statement.setString(3, c.getUserId());
                        p_statement.setString(4, c.getCreatorId());
                        p_statement.executeUpdate();
                    }
                    catch(SQLException ex){
                        ex.printStackTrace();
                    }
                }
                else{
                    throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static void setAsCompleted(Object computation, Object endDate) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                if(endDate instanceof Integer date){
                    String command = "UPDATE computations SET endDate = ? WHERE netId = ? AND userId = ? AND creatorId = ?";

                    try (Connection connection = DatabaseManager.getDBConnection();
                         PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                         p_statement.setInt(1, date);
                        p_statement.setString(2, c.getNetId());
                        p_statement.setString(3, c.getUserId());
                        p_statement.setString(4, c.getCreatorId());
                        p_statement.executeUpdate();
                    }
                    catch(SQLException ex){
                        ex.printStackTrace();
                    }
                }
                else{
                    throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
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

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, net.getNetName());
                    p_statement.setString(2, net.getCreatorId());
                    ResultSet result = p_statement.executeQuery();

                    while (result.next()) {
                        wantedComputations.add(new Computation(
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                result.getLong(5),
                                result.getLong(6)
                        ));
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
        return wantedComputations;
    }

    public static int getIdByComputation(Object computation) throws InputTypeException{
        try {
            if (computation instanceof Computation c) {
                String command = "SELECT id FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, c.getNetId());
                    p_statement.setString(2, c.getCreatorId());
                    p_statement.setString(3,c.getUserId());
                    ResultSet result = p_statement.executeQuery();

                    if(result.next()){
                        return result.getInt(1);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
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

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, u.getUsername());
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        wantedComputations.add( new Computation(
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                result.getInt(5),
                                result.getInt(6)
                        ));
                    }
                }
                catch(SQLException ex){
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

        return wantedComputations;
    }

    public static List<Computation> getComputationsByAdmin(Object user) throws InputTypeException{
        List<Computation> wantedComputations = new ArrayList<Computation>();
        try{
            if(user instanceof User u){
                String command = "SELECT * FROM computations WHERE Id = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, u.getUsername());
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        wantedComputations.add( new Computation(
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                result.getLong(5),
                                result.getLong(6)
                        ));
                    }
                }
                catch(SQLException ex){
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
        return wantedComputations;
    }

    public static List<Computation> getComputationsByAdminAndUser(Object user, Object admin) throws InputTypeException{
        List<Computation> wantedComputations = new ArrayList<Computation>();
        try{
            if(user instanceof User u){
                if(admin instanceof User a) {
                    String command = "SELECT * FROM computations WHERE userId = ? AND creatorId = ?";

                    try (Connection connection = DatabaseManager.getDBConnection();
                         PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                         p_statement.setString(1, u.getUsername());
                        p_statement.setString(1, a.getUsername());
                        ResultSet result = p_statement.executeQuery();

                        while (result.next()) {
                            wantedComputations.add( new Computation(
                                    result.getString(2),
                                    result.getString(3),
                                    result.getString(4),
                                    result.getLong(5),
                                    result.getLong(6)
                            ));
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                else{
                    throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
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

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, net.getNetName());
                    ResultSet result = p_statement.executeQuery();
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return subscribedUsers;
    }

    public static List<PetriNet> getNetsSubscribedByUser(Object user) throws InputTypeException{
        String command = "SELECT netId FROM computations WHERE userId = ? ";
        List <PetriNet> wantedNets = new ArrayList<PetriNet>();
        try{
            if(user instanceof User u){
                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, u.getUsername());
                    ResultSet result = p_statement.executeQuery();

                    while(result.next()) {
                        wantedNets.add(PetriNetsDAO.getNetByName(result.getString(1))
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch (InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedNets;

    }

    public static List<RecentNet> getNetsSubscribedWithTimestampByUser(Object user) throws InputTypeException{
        String command = "SELECT pn.*, MAX(cs.timestamp) " +
                "FROM petri_nets pn " +
                "JOIN computations c ON pn.netName = c.netId " +
                "LEFT JOIN computationSteps cs ON c.id = cs.computationId " +
                "WHERE c.userId = ? " +
                "ORDER BY pn.netName, cs.timestamp;";
        List <RecentNet> wantedNets = new ArrayList<RecentNet>();
        try{
            if(user instanceof User u){
                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, u.getUsername());
                    ResultSet result = p_statement.executeQuery();

                    while(result.next()) {
                        wantedNets.add(new RecentNet(new PetriNet(
                                result.getString(1),
                                result.getString(2),
                                result.getLong(3),
                                result.getString(4),
                                result.getString(5),
                                result.getBoolean(6)
                        ), result.getLong(7)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch (InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedNets;

    }

}
