package it.petrinet.model.Database;
import it.petrinet.model.Notification;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;
import org.controlsfx.control.Notifications;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationsDAO implements DataAccessObject{

    //private final String dbURL = "jdbc:sqlite:/home/davide/IdeaProjects/PetriNetProject2025/notifications.db";

//    public static void main(String[] args) {
//        final String dbURL = "jdbc:sqlite:/home/davide/IdeaProjects/PetriNetProject2025/notifications.db";
//        String sql = "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT);";
//
//        try {
//            Class.forName("org.sqlite.JDBC"); // Carica il driver
//            Connection conn = DriverManager.getConnection(dbURL);
//            Statement stmt = conn.createStatement();
//            stmt.execute(sql);
//            System.out.println("Tabella creata con successo");
//            stmt.close();
//            conn.close();
//        } catch (ClassNotFoundException e) {
//            System.err.println("Driver JDBC non trovato:");
//            e.printStackTrace();
//        } catch (SQLException e) {
//            System.err.println("Errore SQL:");
//            e.printStackTrace();
//        }
//    }

    public void createTable(){
        String table = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "recipient TEXT NOT NULL, " +
                "netId INTEGER NOT NULL, " +
                "type INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL)";
        ;
        try (Connection conn = DatabaseManager.getUserDBConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertNotification(Notification notification){
        String command = "INSERT INTO notifications(sender, recipient, netId, type, title, text, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";

        if (!DatabaseManager.tableExists("notifications", "notifications")){
            NotificationsDAO dao = new NotificationsDAO();
            dao.createTable();
        }

        try (Connection connection = DatabaseManager.getNotificationDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setString(1, notification.getSender());
            p_statement.setString(2, notification.getRecipient());
            p_statement.setInt(3, notification.getNetId());
            p_statement.setInt(4, notification.getType());
            p_statement.setString(5, notification.getTitle());
            p_statement.setString(6, notification.getText());
            p_statement.setInt(7, notification.getTimestamp());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteNotificationsFromNet(PetriNet net){
        String command = "DELETE FROM notifications WHERE netId = ?";

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)){
            p_statement.setInt(1, net.getNetId());
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteNotificationsByRecipient(Object recipient){
        if(recipient instanceof User user) {
            String command = "DELETE FROM notifications WHERE recipient = ?";

            try (Connection connection = DatabaseManager.getUserDBConnection();
                 PreparedStatement p_statement = connection.prepareStatement(command)){
                p_statement.setString(1, user.getUsername());
                p_statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Notification> getNotificationsBySender(User sender){
        List <Notification> filteredNotifications = new ArrayList<Notification>();
        if(sender instanceof User user) {
            if (UserDAO.getUserByUsername(user.getUsername()) == null) {
                System.out.println("No result for indicated user. " + sender + " doesn't exist.");
                return filteredNotifications;
            }
            String command = "SELECT FROM notifications WHERE sender = ? ";

            try (Connection connection = DatabaseManager.getUserDBConnection();
                 PreparedStatement p_statement = connection.prepareStatement(command)) {
                p_statement.setString(1, user.getUsername());
                ResultSet result = p_statement.executeQuery();
                while(result.next()){
                    filteredNotifications.add(new Notification(
                            result.getString("sender"),
                            result.getString("recipient"),
                            result.getInt("netId"),
                            result.getInt("type"),
                            result.getString("title"),
                            result.getString("text"),
                            result.getInt("timestamp")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Input for get function wrong.");
        }
        return filteredNotifications;
    }

    public List<Notification> getNotificationsByRecipient(Object recipient){
        List<Notification> filteredNotifications = new ArrayList<Notification>();
        if(recipient instanceof User user) {
            if (UserDAO.getUserByUsername(user.getUsername()) == null) {
                System.out.println("No result for indicated user. " + recipient + " doesn't exist.");
                return filteredNotifications;
            }
            String command = "SELECT FROM notifications WHERE recipient = ? ";

            try (Connection connection = DatabaseManager.getUserDBConnection();
                 PreparedStatement p_statement = connection.prepareStatement(command)) {
                p_statement.setString(1, user.getUsername());
                ResultSet result = p_statement.executeQuery();
                while(result.next()){
                    filteredNotifications.add(new Notification(
                            result.getString("sender"),
                            result.getString("recipient"),
                            result.getInt("netId"),
                            result.getInt("type"),
                            result.getString("title"),
                            result.getString("text"),
                            result.getInt("timestamp")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Input for get-function has wrong type.");
        }
        return filteredNotifications;
    }

    public List<Notification> getNotificationsbyType(Object type){
        List<Notification> filteredNotifications = new ArrayList<Notification>();

        String command = "SELECT FROM notifications WHERE type = ? ";

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setString(1, (String) type);
            ResultSet result = p_statement.executeQuery();
            while(result.next()){
                filteredNotifications.add(new Notification(
                        result.getString("sender"),
                        result.getString("recipient"),
                        result.getInt("netId"),
                        result.getInt("type"),
                        result.getString("title"),
                        result.getString("text"),
                        result.getInt("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filteredNotifications;
    }

    public List<Notification> getNotificationsByTimestamp(int timestamp){
        List<Notification> filteredNotifications = new ArrayList<Notification>();

        String command = "SELECT FROM notifications WHERE timestamp = ? ";

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setInt(1, timestamp);
            ResultSet result = p_statement.executeQuery();
            while(result.next()){
                filteredNotifications.add(new Notification(
                        result.getString("sender"),
                        result.getString("recipient"),
                        result.getInt("netId"),
                        result.getInt("type"),
                        result.getString("title"),
                        result.getString("text"),
                        result.getInt("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filteredNotifications;
    }

}