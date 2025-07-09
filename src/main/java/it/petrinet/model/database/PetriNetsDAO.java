package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PetriNetsDAO implements DataAccessObject{
    public static void main(String[] args) {
        //insertNet(new PetriNet("net10", "Davide", 1672435200L,"XML", "image", true));
        //insertNet(new PetriNet("net16", "a", 0L,"XML", "image", true));
//        insertNet(new PetriNet("net2", "ale", 456787654L,"XML", "image", true));
//        insertNet(new PetriNet("net3", "ale", 1712924800L,"XML", "image", true));
        insertNet(new PetriNet("net14", "ale", 1710914120L,"XML", "image", true));
        System.out.println(getNetsWithTimestampByCreator(new User("a", "a", true)));
    }

    public void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS petri_nets (" +
                "netName TEXT PRIMARY KEY, " +
                "creatorId TEXT NOT NULL, " +
                "creationDate LONG NOT NULL, " +
                "XML_PATH TEXT NOT NULL, " +
                "image_PATH TEXT NOT NULL, " +
                "isReady BOOLEAN NOT NULL, " +
                "FOREIGN KEY(creatorId) REFERENCES users(username))";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteTable(){
        String command = "DROP TABLE petri_nets;";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(command);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertNet(PetriNet net) {                //c'è da rimpiazzare i placeholder coi get della classe delle reti
        String command = "INSERT INTO petri_nets(netName, creatorId, creationDate, XML_PATH, image_PATH, isReady ) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){

            if(!DatabaseManager.tableExists("petri_nets")){
                PetriNetsDAO dao = new PetriNetsDAO();
                dao.createTable();
            }

             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setString(1, net.getNetName());
            p_statement.setString(2, net.getCreatorId());
            p_statement.setLong(3,net.getCreationDate());
            p_statement.setString(4, net.getXML_PATH());
            p_statement.setString(5, net.getImage_PATH());
            p_statement.setBoolean(6, net.isReady());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeNet(PetriNet net) {
        String command = "DELETE FROM petri_nets WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setString(1, net.getNetName());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getAdminNameByNetName(String netName) {
        String adminName = null;
        String command = "SELECT p.creatorId FROM petri_nets p WHERE netName = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON;");
            p_statement.setString(1, netName);
            ResultSet result = p_statement.executeQuery();
            if(result.next()){
               adminName = result.getString(1);
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
        return adminName;
    }

    public static void setReady(PetriNet net) {
        String command = "UPDATE petri_nets SET isReady = ? WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setBoolean(1, true);
            p_statement.setString(2, net.getNetName());
            p_statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getXMLPATHbyNetName(String netName) {
        String path = null;
        String command = "SELECT p.XML_PATH FROM petri_nets p WHERE netName = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON;");
            p_statement.setString(1, netName);
            ResultSet result = p_statement.executeQuery();
            if(result.next()){
                path = result.getString(1);
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
        return path;
    }

    public static void changeImage(PetriNet net, String newDir) {
        String command = "UPDATE petri_nets SET image_PATH = ? WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setString(1, newDir);
            p_statement.setString(2, net.getNetName());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<PetriNet> getNetsByCreator(User user) {
        List<PetriNet> wantedNets = new ArrayList<PetriNet>();
        String command = "SELECT * FROM petri_nets WHERE creatorId = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setString(1, user.getUsername());
            ResultSet result = p_statement.executeQuery();
            while(result.next()){
                wantedNets.add( new PetriNet(
                        result.getString(1),
                        result.getString(2),
                        result.getLong(3),
                        result.getString(4),
                        result.getString(5),
                        result.getBoolean(6)
                ));
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
        return wantedNets;
    }

    public static List<RecentNet> getNetsWithTimestampByCreator(User user) {
        List<RecentNet> wantedNets = new ArrayList<RecentNet>();
                String command = "SELECT pn.*, MAX(s.timestamp) AS lastTimestamp, c.* FROM petri_nets pn " +
                        "LEFT JOIN computations c ON c.netId = pn.netName " +
                        "LEFT JOIN computationSteps s ON s.computationId = c.id " +
                        "WHERE pn.creatorId = ? " +
                        "GROUP BY pn.netName " +
                        "ORDER BY lastTimestamp DESC;";
                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, user.getUsername());
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        int type = result.getInt(14);
                        if(result.wasNull()){
                            type = 3;
                        }
                        RecentNet rn = new RecentNet(new PetriNet(
                                result.getString(1),
                                result.getString(2),
                                result.getLong(3),
                                result.getString(4),
                                result.getString(5),
                                result.getBoolean(6)
                        ), result.getLong(7)
                        );
                        int id = result.getInt(8);
                        if(!result.wasNull()){
                            rn.setComputation(new Computation(
                                            result.getString(9),
                                            result.getString(10),
                                            result.getString(11),
                                            result.getLong(12),
                                            result.getLong(13),
                                            Computation.toNextStepType(type)
                                    )
                            );
                        }
                        wantedNets.add(rn);
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
        return wantedNets;
    }

    public static PetriNet getNetByName(String name) {
        String command = "SELECT * FROM petri_nets WHERE netName = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setString(1, name);
            ResultSet result = p_statement.executeQuery();
            if(result.next()){
                return new PetriNet(
                        result.getString(1),
                        result.getString(2),
                        result.getLong(3),
                        result.getString(4),
                        result.getString(5),
                        result.getBoolean(6)
                );
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static List<RecentNet> getMostRecentlyModifiedNets(User user, int howMany) {
        List<RecentNet> wantedNets = new ArrayList<RecentNet>();

        String searchCommand = "SELECT pn.*, cs.timestamp, c.* " +
                "FROM petri_nets pn " +
                "LEFT JOIN computations c ON c.netId = pn.netName " +
                "LEFT JOIN computationSteps cs ON cs.computationId = c.id " +
                "WHERE pn.creatorId = ? OR c.userId = ? " +
                "GROUP BY pn.netName " +
                "ORDER BY MAX(cs.timestamp) DESC;";
                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(searchCommand);
                    Statement statement = connection.createStatement()){
                    statement.execute("PRAGMA foreign_keys = ON;");
                    p_statement.setString(1, user.getUsername());
                    p_statement.setString(2, user.getUsername());
                    ResultSet result = p_statement.executeQuery();

                    while(result.next() & howMany>0){
                        RecentNet rn = new RecentNet(
                                new PetriNet( result.getString(1),
                                        result.getString(2),
                                        result.getLong(3),
                                        result.getString(4),
                                        result.getString(5),
                                        result.getBoolean(6))
                                ,result.getLong(7)
                        );

                        int id = result.getInt(8);
                        if(!result.wasNull()){
                            rn.setComputation(new Computation(result.getString(9),
                                    result.getString(10),
                                    result.getString(11),
                                    result.getLong(12),
                                    result.getLong(13),
                                    Computation.toNextStepType(result.getInt(14))));
                        }
                        wantedNets.add(rn);
                        howMany --;
                    }

                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
        return wantedNets;
    }

    /**
     * Retrieves a list of discoverable Petri nets for a given user.
     * A Petri net is discoverable if:
     * 1. The user is not its creator.
     * 2. The Petri net is marked as 'ready'.
     * 3. There is no existing computation for this Petri net associated with the given user.
     *
     * @param user The user for whom to discover Petri nets.
     * @return A list of discoverable PetriNet objects.
     * @ If the provided user object is null.
     */
    public static List<PetriNet> getDiscoverableNetsByUser(User user)  {
        List<PetriNet> discoverableNets = new ArrayList<>();

        String userId = user.getUsername(); // Or user.getUsername() if that's the ID

        String command = "SELECT pn.* " +
                "FROM petri_nets pn " +
                "LEFT JOIN computations c ON pn.netName = c.netId AND c.userId = ? " +
                "WHERE pn.creatorId != ? AND pn.isReady = 1 AND c.netId IS NULL";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {

            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }

            p_statement.setString(1, userId); // For c.userId = ? in LEFT JOIN
            p_statement.setString(2, userId); // For pn.creatorId != ? in WHERE

            try (ResultSet result = p_statement.executeQuery()) {
                while (result.next()) {
                    discoverableNets.add(new PetriNet(
                            result.getString(1),
                            result.getString(2),
                            result.getLong(3),
                            result.getString(4),
                            result.getString(5),
                            result.getBoolean(6)
                    ));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Database error while fetching discoverable Petri nets: " + ex.getMessage());
            ex.printStackTrace(); // Log the full stack trace for debugging
            // Depending on your application's error handling, you might rethrow a custom
            // runtime exception or a checked exception here.
        }
        return discoverableNets;
    }
}

