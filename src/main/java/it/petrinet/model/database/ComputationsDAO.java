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
        insertComputation(new Computation("net3","ale","a", 34,-1 ,Computation.NEXT_STEP_TYPE.ADMIN));
        insertComputation(new Computation("net10","Davide","a", 34, Computation.NEXT_STEP_TYPE.BOTH));
        insertComputation(new Computation("net3","ale","Davide", 34, Computation.NEXT_STEP_TYPE.USER));
        System.out.println(getComputationsByNet(new PetriNet("net3", "ale", 1712924800L, "XML", "image", true)));
    }

    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS computations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "netId TEXT NOT NULL, " +
                "creatorId TEXT NOT NULL, " +
                "userId TEXT NOT NULL," +
                "startDate longINT, " +
                "endDate longINT, " +
                "nextStep INTEGER NOT NULL, " +
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
                String command = "INSERT INTO computations(netId, creatorId, userId, startDate, endDate, nextStep) VALUES (?, ?, ?, ?, ?, ?)";

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
                    p_statement.setInt(6, c.getNextStepType().ordinal());
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
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                result.getLong(5),
                                result.getLong(6),
                                Computation.toNextStepType(result.getInt(7))

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

    private static void removeComputation(int id) throws InputTypeException{
        String command = "DELETE FROM computations WHERE id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setInt(1, id);
            p_statement.executeUpdate();

            ComputationStepDAO.removeAllStepsByComputation(ComputationsDAO.getComputationById(id));
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void removeComputation(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                removeComputation(getIdByComputation(c));
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    //TODO CHECK
    public static void resetComputation(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                String command = "Update computations SET endDate = ? WHERE id = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command);
                     Statement statement = connection.createStatement()){
                    statement.execute("PRAGMA foreign_keys = ON;");
                    p_statement.setNull(1, Types.BIGINT);
                    p_statement.setInt(2, getIdByComputation(c));
                    p_statement.executeUpdate();

                    ComputationStepDAO.resetComputationSteps(c);
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
    //TODO CHECK
    public static void deleteComputation(Object computation) throws InputTypeException{
        try{
            if(computation instanceof Computation c){
                String command = "Update computations SET endDate = ?, startDate = ? WHERE id = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command);
                     Statement statement = connection.createStatement()){
                    statement.execute("PRAGMA foreign_keys = ON;");
                    p_statement.setNull(1, Types.BIGINT);
                    p_statement.setNull(2, Types.BIGINT);
                    p_statement.setInt(3, getIdByComputation(c));
                    p_statement.executeUpdate();

                    ComputationStepDAO.removeAllStepsByComputation(c);
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
                String command = "SELECT * FROM computations WHERE netId = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command);
                     Statement statement = connection.createStatement()){

                    statement.execute("PRAGMA foreign_keys = ON;");
                    p_statement.setString(1, net.getNetName());

                    ResultSet result = p_statement.executeQuery();

                    while (result.next()) {
                        long sDate = result.getLong(5);
                        if(result.wasNull()){
                            sDate = -1L;
                        }
                        long eDate = result.getLong(6);
                        if(result.wasNull()){
                            eDate = -1L;
                        }
                        wantedComputations.add(new Computation(
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                sDate,
                                eDate,
                                Computation.toNextStepType(result.getInt(7))
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
                        long sDate = result.getLong(5);
                        if(result.wasNull()){
                            sDate = -1L;
                        }
                        long eDate = result.getLong(6);
                        if(result.wasNull()){
                            eDate = -1L;
                        }
                        wantedComputations.add( new Computation(
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                eDate,
                                sDate,
                                Computation.toNextStepType(result.getInt(7))
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
                        long sDate = result.getLong(5);
                        if(result.wasNull()){
                            sDate = -1L;
                        }
                        long eDate = result.getLong(6);
                        if(result.wasNull()){
                            eDate = -1L;
                        }
                        wantedComputations.add( new Computation(
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                sDate,
                                eDate,
                                Computation.toNextStepType(result.getInt(7))
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
                            long sDate = result.getLong(5);
                            if(result.wasNull()){
                                sDate = -1L;
                            }
                            long eDate = result.getLong(6);
                            if(result.wasNull()){
                                eDate = -1L;
                            }
                            wantedComputations.add( new Computation(
                                    result.getString(2),
                                    result.getString(3),
                                    result.getString(4),
                                    sDate,
                                    eDate,
                                    Computation.toNextStepType(result.getInt(7))
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

    public static Computation getComputationByUserAndNet(Object user, Object net) throws InputTypeException{
        try{
            if(user instanceof User u){
                if(net instanceof PetriNet n) {
                    String command = "SELECT * FROM computations WHERE userId = ? AND netId = ?";

                    try (Connection connection = DatabaseManager.getDBConnection();
                         PreparedStatement p_statement = connection.prepareStatement(command);
                         Statement statement = connection.createStatement()){
                        statement.execute("PRAGMA foreign_keys = ON;");
                        p_statement.setString(1, u.getUsername());
                        p_statement.setString(2, n.getNetName());
                        ResultSet result = p_statement.executeQuery();

                        if (result.next()) {
                            long sDate = result.getLong(5);
                            if(result.wasNull()){
                                sDate = -1L;
                            }
                            long eDate = result.getLong(6);
                            if(result.wasNull()){
                                eDate = -1L;
                            }

                            return new Computation(
                                    result.getString(2),
                                    result.getString(3),
                                    result.getString(4),
                                    sDate,
                                    eDate,
                                    Computation.toNextStepType(result.getInt(7))
                            );
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
        return null;
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

    public static List<RecentNet> getRecentNetsSubscribedByUser(Object user) throws InputTypeException{
        String command = "SELECT pn.*, MAX(cs.timestamp), c.* " +
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
                            ), result.getLong(7),
                                new Computation(result.getString(9),
                                        result.getString(10),
                                        result.getString(11),
                                        result.getLong(12),
                                        result.getLong(13),
                                        Computation.toNextStepType(result.getInt(14)))
                                )
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

}
