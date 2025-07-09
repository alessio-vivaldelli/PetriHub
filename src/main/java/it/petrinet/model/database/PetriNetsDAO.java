package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;
import javafx.scene.chart.PieChart;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing PetriNets in the database.
 */
public class PetriNetsDAO implements DataAccessObject {

    /**
     * Creates the petri_nets table if it does not already exist.
     */
    public void createTable() {
        String table = """
            CREATE TABLE IF NOT EXISTS petri_nets (
                netName TEXT PRIMARY KEY,
                creatorId TEXT NOT NULL,
                creationDate LONG NOT NULL,
                XML_PATH TEXT NOT NULL,
                image_PATH TEXT NOT NULL,
                isReady BOOLEAN NOT NULL,
                FOREIGN KEY(creatorId) REFERENCES users(username) ON DELETE CASCADE
            )""";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            DatabaseManager.enableForeignKeys(statement);
            statement.execute(table);
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("createPetriNetsTable", ex);
        }
    }

    /**
     * Deletes the petri_nets table.
     */
    public static void deleteTable() {
        String command = "DROP TABLE petri_nets;";
        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(command);

        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("deleteTable", ex);
        }
    }

    /**
     * Inserts a new PetriNet into the database.
     *
     * @param net the PetriNet object to insert
     */
    public static void insertNet(PetriNet net) {
        String command = """
            INSERT INTO petri_nets(netName, creatorId, creationDate, XML_PATH, image_PATH, isReady)
            VALUES (?, ?, ?, ?, ?, ?)""";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            if (!DatabaseManager.tableExists("petri_nets")) {
                new PetriNetsDAO().createTable();
            }

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, net.getNetName());
            ps.setString(2, net.getCreatorId());
            ps.setLong(3, net.getCreationDate());
            ps.setString(4, net.getXML_PATH());
            ps.setString(5, net.getImage_PATH());
            ps.setBoolean(6, net.isReady());
            ps.executeUpdate();

        } catch (SQLException e) {
            DatabaseManager.handleSQLException("insertNet", e);
        }
    }

    /**
     * Deletes the given PetriNet from the database.
     *
     * @param net the PetriNet to remove
     */
    public static void removeNet(PetriNet net) {
        String command = "DELETE FROM petri_nets WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, net.getNetName());
            ps.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("removeNet", e);
        }
    }

    /**
     * Returns the creator (admin) username of a given PetriNet.
     *
     * @param netName the name of the PetriNet
     * @return the creator's username, or null if not found
     */
    public static String getAdminNameByNetName(String netName) {
        String query = "SELECT creatorId FROM petri_nets WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, netName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getAdminNameByNetName", e);
        }
        return null;
    }

    /**
     * Marks the given PetriNet as ready.
     *
     * @param net the PetriNet to mark
     */
    public static void setReady(PetriNet net) {
        String query = "UPDATE petri_nets SET isReady = ? WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setBoolean(1, true);
            ps.setString(2, net.getNetName());
            ps.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("setReady", e);
        }
    }

    /**
     * Retrieves the XML path for a given PetriNet.
     *
     * @param netName the name of the PetriNet
     * @return the XML path, or null if not found
     */
    public static String getXMLPATHByNetName(String netName) {
        String query = "SELECT XML_PATH FROM petri_nets WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, netName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getXMLPATHByNetName", e);
        }
        return null;
    }

    /**
     * Updates the image path for the given PetriNet.
     *
     * @param net    the PetriNet to update
     * @param newDir the new image path
     */
    public static void changeImage(PetriNet net, String newDir) {
        String query = "UPDATE petri_nets SET image_PATH = ? WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, newDir);
            ps.setString(2, net.getNetName());
            ps.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("changeImage", e);
        }
    }

    /**
     * Returns all PetriNets created by the given user.
     *
     * @param user the user to filter by
     * @return list of PetriNets
     */
    public static List<PetriNet> getNetsByCreator(User user) {
        List<PetriNet> nets = new ArrayList<>();
        String query = "SELECT * FROM petri_nets WHERE creatorId = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, user.getUsername());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nets.add(new PetriNet(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getLong(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getBoolean(6)
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getNetsByCreator", e);
        }
        return nets;
    }

    /**
     * Returns PetriNets created by a user, including timestamp and computation metadata.
     *
     * @param user the user
     * @return list of RecentNet
     */
    public static List<RecentNet> getNetsWithTimestampByCreator(User user) {
        List<RecentNet> nets = new ArrayList<>();
        String query = """
            SELECT pn.*, MAX(s.timestamp) AS lastTimestamp, c.* FROM petri_nets pn
            LEFT JOIN computations c ON c.netId = pn.netName
            LEFT JOIN computationSteps s ON s.computationId = c.id
            WHERE pn.creatorId = ?
            GROUP BY pn.netName
            ORDER BY lastTimestamp DESC""";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, user.getUsername());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int type = rs.getInt(14);
                if (rs.wasNull()) type = 3;

                RecentNet rn = new RecentNet(new PetriNet(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getLong(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getBoolean(6)
                ), rs.getLong(7));

                if (!rs.wasNull() && rs.getInt(8) != 0) {
                    rn.setComputation(new Computation(
                            rs.getString(9),
                            rs.getString(10),
                            rs.getString(11),
                            rs.getLong(12),
                            rs.getLong(13),
                            Computation.toNextStepType(type)
                    ));
                }

                nets.add(rn);
            }
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("getNetsWithTimestampByCreator", ex);
        }
        return nets;
    }

    /**
     * Fetches a PetriNet by its name.
     *
     * @param name the PetriNet name
     * @return the PetriNet or null
     */
    public static PetriNet getNetByName(String name) {
        String query = "SELECT * FROM petri_nets WHERE netName = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new PetriNet(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getLong(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getBoolean(6)
                );
            }
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("getNetByName", ex);
        }
        return null;
    }

    /**
     * Returns the most recently modified PetriNets for a user.
     *
     * @param user    the user
     * @param howMany how many results to return
     * @return list of RecentNet
     */
    public static List<RecentNet> getMostRecentlyModifiedNets(User user, int howMany) {
        List<RecentNet> nets = new ArrayList<>();
        String query = """
            SELECT pn.*, cs.timestamp, c.* 
            FROM petri_nets pn 
            LEFT JOIN computations c ON c.netId = pn.netName 
            LEFT JOIN computationSteps cs ON cs.computationId = c.id 
            WHERE pn.creatorId = ? OR c.userId = ? 
            GROUP BY pn.netName 
            ORDER BY MAX(cs.timestamp) DESC""";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getUsername());
            ResultSet rs = ps.executeQuery();

            while (rs.next() && howMany-- > 0) {
                RecentNet rn = new RecentNet(
                        new PetriNet(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getLong(3),
                                rs.getString(4),
                                rs.getString(5),
                                rs.getBoolean(6)),
                        rs.getLong(7)
                );

                if (!rs.wasNull() && rs.getInt(8) != 0) {
                    rn.setComputation(new Computation(
                            rs.getString(9),
                            rs.getString(10),
                            rs.getString(11),
                            rs.getLong(12),
                            rs.getLong(13),
                            Computation.toNextStepType(rs.getInt(14))
                    ));
                }

                nets.add(rn);
            }
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("getMostRecentlyModifiedNets", ex);
        }
        return nets;
    }

    /**
     * Returns discoverable PetriNets for a user:
     * - Not created by the user.
     * - Marked as ready.
     * - Not already computed by the user.
     *
     * @param user the user
     * @return list of PetriNets
     */
    public static List<PetriNet> getDiscoverableNetsByUser(User user) {
        List<PetriNet> nets = new ArrayList<>();
        String query = """
            SELECT pn.* 
            FROM petri_nets pn 
            LEFT JOIN computations c ON pn.netName = c.netId AND c.userId = ? 
            WHERE pn.creatorId != ? AND pn.isReady = 1 AND c.netId IS NULL""";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             Statement stmt = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(stmt);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getUsername());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                nets.add(new PetriNet(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getLong(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getBoolean(6)
                ));
            }
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("getDiscoverableNetsByUser", ex);
        }
        return nets;
    }
}
