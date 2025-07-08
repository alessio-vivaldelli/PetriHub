package it.petrinet.model.database;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Notification;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationsDAO implements DataAccessObject{
    public static void main(String args[]) throws InputTypeException {
        NotificationsDAO not = new NotificationsDAO();
        not.createTable();
    }

    public void createTable(){
        String table = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "recipient TEXT NOT NULL, " +
                "netId TEXT NOT NULL, " +
                "type INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "UNIQUE(sender, recipient, timestamp), " +
                "FOREIGN KEY(sender) REFERENCES users(username), " +
                "FOREIGN KEY(recipient) REFERENCES users(username), " +
                "FOREIGN KEY(netId) REFERENCES petri_nets(netName)" +
                ")";
        ;
        try (Connection conn = DatabaseManager.getDBConnection();
             Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteTable(){
        String command = "DROP TABLE notifications;";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.execute(command);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertNotification(Object notification) throws InputTypeException {
        String command = "INSERT INTO notifications(sender, recipient, netId, type, title, text, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            if (notification instanceof Notification not) {
                try{
                    if (!DatabaseManager.tableExists("notifications")) {
                        NotificationsDAO dao = new NotificationsDAO();
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
                     p_statement.setString(1, not.getSender());
                    p_statement.setString(2, not.getRecipient());
                    p_statement.setString(3, not.getNetId());
                    p_statement.setInt(4, not.getType());
                    p_statement.setString(5, not.getTitle());
                    p_statement.setString(6, not.getText());
                    p_statement.setInt(7, not.getTimestamp());
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

    public static void deleteNotificationsFromNet(Object net) throws InputTypeException{
        String command = "DELETE FROM notifications WHERE netId = ?";
        try{
            if(net instanceof PetriNet pNet){
                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, pNet.getNetName());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }

    }

    public static void deleteNotificationsByRecipient(Object recipient) throws InputTypeException{
        try{
            if(recipient instanceof User user) {
                String command = "DELETE FROM notifications WHERE recipient = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, user.getUsername());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch (InputTypeException e){
            e.ErrorPrinter();
        }

    }

    public static List<Notification> getNotificationsBySender(Object sender) throws InputTypeException{
        List <Notification> filteredNotifications = new ArrayList<Notification>();
        try{
            if(sender instanceof User user) {

                if (UserDAO.getUserByUsername(user.getUsername()) == null) {
                    System.out.println("No result for indicated user. " + sender + " doesn't exist.");
                    return filteredNotifications;
                }
                String command = "SELECT * FROM notifications WHERE sender = ? ";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, user.getUsername());
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        filteredNotifications.add(new Notification(
                                result.getString("sender"),
                                result.getString("recipient"),
                                result.getString("netId"),
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch (InputTypeException e){
            e.ErrorPrinter();
        }
        return filteredNotifications;
    }

    public static List<Notification> getNotificationsByRecipient(Object recipient) throws InputTypeException{
        List<Notification> filteredNotifications = new ArrayList<Notification>();
        try{
            if(recipient instanceof User user) {
                if (UserDAO.getUserByUsername(user.getUsername()) == null) {
                    System.out.println("No result for indicated user. " + recipient + " doesn't exist.");
                    return filteredNotifications;
                }
                String command = "SELECT * FROM notifications WHERE recipient = ? ";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, user.getUsername());
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        filteredNotifications.add(new Notification(
                                result.getString("sender"),
                                result.getString("recipient"),
                                result.getString("netId"),
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }

        return filteredNotifications;
    }

    public static List<Notification> getNotificationsByType(Object type){
        List<Notification> filteredNotifications = new ArrayList<Notification>();
        try{
            if(type instanceof Integer t){
                String command = "SELECT * FROM notifications WHERE type = ? ";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setInt(1, t);
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        filteredNotifications.add(new Notification(
                                result.getString("sender"),
                                result.getString("recipient"),
                                result.getString("netId"),
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }

        return filteredNotifications;
    }

    public static List<Notification> getNotificationsByTimestamp(Object timestamp) throws InputTypeException{
        List<Notification> filteredNotifications = new ArrayList<Notification>();
        try{
            if(timestamp instanceof Integer time){
                String command = "SELECT * FROM notifications WHERE timestamp = ? ";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setInt(1, time);
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        filteredNotifications.add(new Notification(
                                result.getString("sender"),
                                result.getString("recipient"),
                                result.getString("netId"),
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return filteredNotifications;
    }

    public static User getRecipientByNotification(Object notification) throws InputTypeException{
        try {
            if (notification instanceof Notification not) {
                String command = "SELECT userId FROM notifications WHERE id = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setInt(1,NotificationsDAO.getIdByNotification(not));
                    ResultSet result = p_statement.executeQuery();

                    if(result.next()){
                        return UserDAO.getUserByUsername(result.getString(1));
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

    public static int getIdByNotification(Object notification) throws InputTypeException{
        try {
            if (notification instanceof Notification not) {
                String command = "SELECT id FROM notifications WHERE sender = and recipient = ? and timestamp = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1,not.getSender());
                    p_statement.setString(2, not.getRecipient());
                    p_statement.setInt(3, not.getTimestamp());
                    ResultSet result = p_statement.executeQuery();

                    if(result.next()){
                        return result.getInt(1);
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
        return -1;
    }

}