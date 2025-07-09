package it.petrinet.model.database;

import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;
import javafx.scene.chart.PieChart;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Data Access Object for managing operations on Computations Table 
 */
public class ComputationsDAO implements DataAccessObject{

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
                "FOREIGN KEY (netId) REFERENCES petri_nets(netName) ON DELETE CASCADE, " +
                "FOREIGN KEY (creatorId) REFERENCES users(username) ON DELETE CASCADE, " +
                "FOREIGN KEY (userId) REFERENCES users(username) ON DELETE CASCADE)";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            DatabaseManager.enableForeignKeys(statement);
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("createComputationsTable", ex);
        }
    }

    public static void deleteTable(){
        String command = "DROP TABLE computations;";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            DatabaseManager.enableForeignKeys(statement);
            statement.execute(command);
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("deleteComputationsTable", ex);
        }
    }

    public static void insertComputation(Computation computation) {
        String command = "INSERT INTO computations(netId, creatorId, userId, startDate, endDate, nextStep) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            if(!DatabaseManager.tableExists("computations")){
                ComputationsDAO dao = new ComputationsDAO();
                dao.createTable();
            }
             DatabaseManager.enableForeignKeys(statement);
             p_statement.setString(1, computation.getNetId());
            p_statement.setString(2, computation.getCreatorId());
            p_statement.setString(3, computation.getUserId());
            if(computation.getStartTimestamp()>0){
                p_statement.setLong(4, computation.getStartTimestamp());
            }
            if(computation.getEndTimestamp()>0){
                p_statement.setLong(5, computation.getEndTimestamp());
            }
            p_statement.setInt(6, computation.getNextStepType().ordinal());
            p_statement.executeUpdate();
        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("insertComputation", ex);
        }
    }

    public static Computation getComputationById(int id){
        String command = "SELECT * FROM computations WHERE id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             DatabaseManager.enableForeignKeys(statement);
             p_statement.setInt(1, id);
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
            DatabaseManager.handleSQLException("getComputationsById", ex);
        }
        return null;
    }

    private static void removeComputation(int id) {
        String command = "DELETE FROM computations WHERE id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            ComputationStepDAO.removeAllStepsByComputation(ComputationsDAO.getComputationById(id));

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setInt(1, id);
            p_statement.executeUpdate();

        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("removeComputation", ex);
        }
    }

    public static void removeComputation(Computation computation) {
        removeComputation(getIdByComputation(computation));
    }

    public static void resetComputation(Computation computation) {
        String command = "Update computations SET endDate = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setNull(1, Types.BIGINT);
            p_statement.setInt(2, getIdByComputation(computation));
            p_statement.executeUpdate();

            ComputationStepDAO.resetComputationSteps(computation);
        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("resetComputation", ex);
        }
    }

    public static void deleteComputation(Computation computation) {
        String command = "Update computations SET endDate = ?, startDate = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            DatabaseManager.enableForeignKeys(statement);
            p_statement.setNull(1, Types.BIGINT);
            p_statement.setNull(2, Types.BIGINT);
            p_statement.setInt(3, getIdByComputation(computation));
            p_statement.executeUpdate();

            ComputationStepDAO.removeAllStepsByComputation(computation);
        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("deleteComputation", ex);
        }
    }

    public static boolean isComplete(Computation computation) {
        String command = "SELECT endDate FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, computation.getNetId());
            p_statement.setString(2, computation.getCreatorId());
            p_statement.setString(3, computation.getUserId());
            ResultSet result = p_statement.executeQuery();

            if(result.next()){
                result.getInt(1);
                return !result.wasNull();   //se è not null, c'è una data inserita, quindi ritorna vero
            }

        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("isComplete", ex);
        }
        return false;
    }

    public static void setAsStarted(Computation computation, long startDate) {
        String command = "UPDATE computations SET startDate = ? WHERE netId = ? AND userId = ? AND creatorId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
            Statement statement = connection.createStatement()){

            DatabaseManager.enableForeignKeys(statement);
            p_statement.setLong(1, startDate);
            p_statement.setString(2, computation.getNetId());
            p_statement.setString(3, computation.getUserId());
            p_statement.setString(4, computation.getCreatorId());
            p_statement.executeUpdate();
        }
        catch(SQLException ex) {
            DatabaseManager.handleSQLException("setAsStarted", ex);
        }
    }

    public static void setAsCompleted(Computation computation, long endDate) {
        String command = "UPDATE computations SET endDate = ? WHERE netId = ? AND userId = ? AND creatorId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
         Statement statement = connection.createStatement()){
         DatabaseManager.enableForeignKeys(statement);
             p_statement.setLong(1, endDate);
            p_statement.setString(2, computation.getNetId());
            p_statement.setString(3, computation.getUserId());
            p_statement.setString(4, computation.getCreatorId());
            p_statement.executeUpdate();
        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("setAsCompleted", ex);
        }
    }

    public static List<Computation> getComputationsByNet(PetriNet net) {
        List<Computation> wantedComputations = new ArrayList<Computation>();
        String command = "SELECT * FROM computations WHERE netId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            DatabaseManager.enableForeignKeys(statement);
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
            DatabaseManager.handleSQLException("getComputationsByNet", ex);
        }
        return wantedComputations;
    }

    public static int getIdByComputation(Computation computation) {
                String command = "SELECT id FROM computations WHERE netId = ? AND creatorId = ? AND userId = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command);
                     Statement statement = connection.createStatement()){

                    DatabaseManager.enableForeignKeys(statement);

                    p_statement.setString(1, computation.getNetId());
                    p_statement.setString(2, computation.getCreatorId());
                    p_statement.setString(3, computation.getUserId());
                    ResultSet result = p_statement.executeQuery();

                    if(result.next()){
                        return result.getInt(1);
                    }
                } catch (SQLException ex) {
                    DatabaseManager.handleSQLException("getIdByComputation", ex);
                }
        return -1;
    }

    public static List<Computation> getComputationsByUser(User user) {
        List<Computation> wantedComputations = new ArrayList<Computation>();
        String command = "SELECT * FROM computations WHERE creatorId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, user.getUsername());
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
            DatabaseManager.handleSQLException("getComputationsByUser", ex);
        }
        return wantedComputations;
    }

    public static List<Computation> getComputationsByAdmin(User user) {
        List<Computation> wantedComputations = new ArrayList<Computation>();
        String command = "SELECT * FROM computations WHERE Id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, user.getUsername());
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
        catch(SQLException ex) {
            DatabaseManager.handleSQLException("getComputationsByAdmin", ex);
        }
        return wantedComputations;
    }

    public static List<Computation> getComputationsByAdminAndUser(User user, User admin) {
        List<Computation> wantedComputations = new ArrayList<Computation>();
        String command = "SELECT * FROM computations WHERE userId = ? AND creatorId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, user.getUsername());
            p_statement.setString(1, admin.getUsername());
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
            DatabaseManager.handleSQLException("getComputationsByAdminAndUser", ex);
        }
        return wantedComputations;
    }

    public static Computation getComputationByUserAndNet(User user, PetriNet net) {
        String command = "SELECT * FROM computations WHERE userId = ? AND netId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            DatabaseManager.enableForeignKeys(statement);
            p_statement.setString(1, user.getUsername());
            p_statement.setString(2, net.getNetName());
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
            DatabaseManager.handleSQLException("getComputationByUserAndNet", ex);
        }
        return null;
    }

    public static List<User> getSubscribedUsersByNet(PetriNet net){
        List<User> subscribedUsers = new ArrayList<User>();
        String command = "SELECT userId FROM computations WHERE netId = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             DatabaseManager.enableForeignKeys(statement);
             p_statement.setString(1, net.getNetName());
            ResultSet result = p_statement.executeQuery();
            while(result.next()){
                subscribedUsers.add(UserDAO.getUserByUsername(result.getString(1))
                );
            }
        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("getSubscribedUsersByNet", ex);
        }
        return subscribedUsers;
    }

    public static List<PetriNet> getNetsSubscribedByUser(User user){
        String command = "SELECT netId FROM computations WHERE userId = ? ";
        List <PetriNet> wantedNets = new ArrayList<PetriNet>();
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             DatabaseManager.enableForeignKeys(statement);
             p_statement.setString(1, user.getUsername());
            ResultSet result = p_statement.executeQuery();

            while(result.next()) {
                wantedNets.add(PetriNetsDAO.getNetByName(result.getString(1))
                );
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getNetsSubscribedByUser", e);
        }
        return wantedNets;

    }

    public static void setNextStepType(Computation computation, int type) {
        String command = "UPDATE computations SET nextStep = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            DatabaseManager.enableForeignKeys(statement);
            p_statement.setInt(1, type);
            p_statement.setInt(2, getIdByComputation(computation));
            p_statement.executeUpdate();
        }
        catch(SQLException ex){
            DatabaseManager.handleSQLException("setNextStepType", ex);
        }
    }

    public static List<Computation> getMostRecentlyModifiedNets(User user, int howMany) {
        List<Computation> nets = new ArrayList<>();
        String query = """
        SELECT c.* 
        FROM computations c 
        LEFT JOIN computationSteps cs ON cs.computationId = c.id 
        WHERE c.userId = ? OR c.creatorId = ?
        GROUP BY c.id 
        ORDER BY MAX(cs.timestamp) DESC 
        LIMIT ?""";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getUsername());
            ps.setInt(3, howMany);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Computation computation = new Computation(
                        rs.getString("netId"),         // or rs.getString(2)
                        rs.getString("creatorId"),  // or rs.getString(3)
                        rs.getString("userId"),       // or rs.getString(3)
                        rs.getLong("startDate"),      // or rs.getLong(4)
                        rs.getLong("endDate"),
                        Computation.toNextStepType(rs.getInt("nextStep"))   // or rs.getLong(5)/ or rs.getInt(6))
                );

                nets.add(computation);
            }
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("getMostRecentlyModifiedNets", ex);
        }

        return nets;
    }

}
