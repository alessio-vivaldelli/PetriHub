package it.petrinet.model.database;

import it.petrinet.model.Notification;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling operations related to notifications in the database.
 */
public class NotificationsDAO implements DataAccessObject {

    public static void main(String args[]) {
        deleteTable();
        NotificationsDAO not = new NotificationsDAO();
        not.createTable();
    }

    /**
     * Creates the notifications table if it does not already exist.
     * Includes foreign key constraints to users and Petri nets.
     */
    public void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "receiver TEXT NOT NULL, " +
                "netId TEXT NOT NULL, " +
                "type INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp LONG NOT NULL, " +
                "FOREIGN KEY(sender) REFERENCES users(username), " +
                "FOREIGN KEY(receiver) REFERENCES users(username), " +
                "FOREIGN KEY(netId) REFERENCES petri_nets(netName) ON DELETE CASCADE" +
                ")";
        try (Connection conn = DatabaseManager.getDBConnection();
             Statement statement = conn.createStatement()) {
            DatabaseManager.enableForeignKeys(statement);
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("createNotificationsTable", ex);
        }
    }

    /**
     * Drops the notifications table from the database.
     */
    public static void deleteTable() {
        String command = "DROP TABLE notifications;";
        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            statement.execute(command);
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("deleteNotificationsTable", ex);
        }
    }

    /**
     * Inserts a new notification into the database.
     *
     * @param notification the notification to insert
     */
    public static void insertNotification(Notification notification) {
        String command = "INSERT INTO notifications(sender, receiver, netId, type, title, text, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            if (!DatabaseManager.tableExists("notifications")) {
                NotificationsDAO dao = new NotificationsDAO();
                dao.createTable();
            }

            DatabaseManager.enableForeignKeys(statement);
            p_statement.setString(1, notification.getSender());
            p_statement.setString(2, notification.getReceiver());
            p_statement.setString(3, notification.getNetId());
            p_statement.setInt(4, notification.getType());
            p_statement.setString(5, notification.getTitle());
            p_statement.setString(6, notification.getText());
            p_statement.setLong(7, notification.getTimestamp());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("insertNotification", e);
        }
    }

    /**
     * Removes all notifications related to a specific Petri net.
     *
     * @param net the Petri net whose notifications should be removed
     */
    public static void removeNotificationsFromNet(PetriNet net) {
        String command = "DELETE FROM notifications WHERE netId = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, net.getNetName());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("removeNotificationsFromNet", e);
        }
    }

    /**
     * Removes all notifications received by a specific user.
     *
     * @param receiver the user who received the notifications
     */
    public static void removeNotificationsByReceiver(User receiver) {
        String command = "DELETE FROM notifications WHERE receiver = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, receiver.getUsername());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("removeNotificationsByReceiver", e);
        }
    }

    /**
     * Retrieves all notifications received by a specific user.
     *
     * @param receiver the user who received the notifications
     * @return list of notifications received
     */
    public static List<Notification> getNotificationsByReceiver(User receiver) {
        List<Notification> filteredNotifications = new ArrayList<>();
        String command = "SELECT * FROM notifications WHERE receiver = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, receiver.getUsername());
            ResultSet result = p_statement.executeQuery();
            while (result.next()) {
                filteredNotifications.add(new Notification(
                        result.getString("sender"),
                        result.getString("receiver"),
                        result.getString("netId"),
                        result.getInt("type"),
                        result.getString("title"),
                        result.getString("text"),
                        result.getLong("timestamp")));
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getNotificationsByReceiver", e);
        }
        return filteredNotifications;
    }

    /**
     * Retrieves all notifications of a given type.
     *
     * @param type the notification type
     * @return list of matching notifications
     */
    public static List<Notification> getNotificationsByType(int type) {
        List<Notification> filteredNotifications = new ArrayList<>();
        String command = "SELECT * FROM notifications WHERE type = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setInt(1, type);
            ResultSet result = p_statement.executeQuery();
            while (result.next()) {
                filteredNotifications.add(new Notification(
                        result.getString("sender"),
                        result.getString("receiver"),
                        result.getString("netId"),
                        result.getInt("type"),
                        result.getString("title"),
                        result.getString("text"),
                        result.getLong("timestamp")));
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getNotificationsByType", e);
        }
        return filteredNotifications;
    }

    /**
     * Retrieves all notifications with a specific timestamp.
     *
     * @param timestamp the notification timestamp
     * @return list of matching notifications
     */
    public static List<Notification> getNotificationsByTimestamp(long timestamp) {
        List<Notification> filteredNotifications = new ArrayList<>();
        String command = "SELECT * FROM notifications WHERE timestamp = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setLong(1, timestamp);
            ResultSet result = p_statement.executeQuery();
            while (result.next()) {
                filteredNotifications.add(new Notification(
                        result.getString("sender"),
                        result.getString("receiver"),
                        result.getString("netId"),
                        result.getInt("type"),
                        result.getString("title"),
                        result.getString("text"),
                        result.getLong("timestamp")));
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getNotificationsByTimestamp", e);
        }
        return filteredNotifications;
    }


    /**
     * Retrieves a notification by the receiver and deletes it from the database.
     *
     * @param receiver the notification receiver
     * @return the removed notification or null
     */
    public static List<Notification> extractNotificationsByReceiver(User receiver) {
        List<Notification> myNots = new ArrayList<Notification>();
        String command = "SELECT * FROM notifications WHERE receiver = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);

            p_statement.setString(1, receiver.getUsername());
            ResultSet result = p_statement.executeQuery();
            while (result.next()) {
                myNots.add(new Notification(
                        result.getString("sender"),
                        result.getString("receiver"),
                        result.getString("netId"),
                        result.getInt("type"),
                        result.getString("title"),
                        result.getString("text"),
                        result.getLong("timestamp")));
            }

            removeNotificationsByReceiver(receiver);
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("extractNotificationById", e);
        }
        return myNots;
    }
}
