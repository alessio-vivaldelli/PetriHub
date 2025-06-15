package it.petrinet.model.database;
import it.petrinet.exceptions.ExceptionType;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Notification;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationsDAO implements DataAccessObject{

    public void createTable(){
        String table = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "recipient TEXT NOT NULL, " +
                "netId TEXT NOT NULL, " +
                "type INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL)";
        ;
        try (Connection conn = DatabaseManager.getNotificationsDBConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertNotification(Object notification) throws InputTypeException {
        String command = "INSERT INTO notifications(sender, recipient, netId, type, title, text, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            if (notification instanceof Notification not) {
                if (!DatabaseManager.tableExists("notifications", "notifications")) {
                    NotificationsDAO dao = new NotificationsDAO();
                    dao.createTable();
                }

                try (Connection connection = DatabaseManager.getNotificationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
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
                throw new InputTypeException(typeErrorMessage, ExceptionType.NOTIFICATION);
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
                try (Connection connection = DatabaseManager.getNotificationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setString(1, pNet.getNetName());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.NOTIFICATION);
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

                try (Connection connection = DatabaseManager.getNotificationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setString(1, user.getUsername());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.NOTIFICATION);
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

                try (Connection connection = DatabaseManager.getNotificationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
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
                throw new InputTypeException(typeErrorMessage, ExceptionType.NOTIFICATION);
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

                try (Connection connection = DatabaseManager.getNotificationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
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
                throw new InputTypeException(typeErrorMessage, ExceptionType.NOTIFICATION);
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

                try (Connection connection = DatabaseManager.getNotificationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
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
                throw new InputTypeException(typeErrorMessage, ExceptionType.NOTIFICATION);
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

                try (Connection connection = DatabaseManager.getNotificationsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
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
                throw new InputTypeException(typeErrorMessage, ExceptionType.NOTIFICATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return filteredNotifications;
    }

}