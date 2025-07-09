package it.petrinet.model.database;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Notification;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationsDAO implements DataAccessObject{
    public static void main(String args[])  {
        deleteTable();
        NotificationsDAO not = new NotificationsDAO();
        not.createTable();
    }

    public void createTable(){
        String table = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "receiver TEXT NOT NULL, " +
                "netId TEXT NOT NULL, " +
                "type INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp LONG NOT NULL, " +
                "UNIQUE(sender, receiver, timestamp), " +
                "FOREIGN KEY(sender) REFERENCES users(username), " +
                "FOREIGN KEY(receiver) REFERENCES users(username), " +
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

    public static void insertNotification(Object notification)  {
        String command = "INSERT INTO notifications(sender, receiver, netId, type, title, text, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
                    p_statement.setString(2, not.getReceiver());
                    p_statement.setString(3, not.getNetId());
                    p_statement.setInt(4, not.getType());
                    p_statement.setString(5, not.getTitle());
                    p_statement.setString(6, not.getText());
                    p_statement.setLong(7, not.getTimestamp());
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
    public static void removeNotification(Notification notification) {
        String command = "DELETE FROM notifications WHERE id = ?";
        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON;");
            p_statement.setInt(1, getIdByNotification(notification));
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeNotificationsFromNet(PetriNet net) {
        String command = "DELETE FROM notifications WHERE netId = ?";
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

    public static void removeNotificationsByReceiver(Object receiver) {
        try{
            if(receiver instanceof User user) {
                String command = "DELETE FROM notifications WHERE receiver = ?";

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

    public static List<Notification> getNotificationsBySender(Object sender) {
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
                                result.getString("receiver"),
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

    public static List<Notification> getNotificationsByReceiver(Object receiver) {
        List<Notification> filteredNotifications = new ArrayList<Notification>();
        try{
            if(receiver instanceof User user) {
                if (UserDAO.getUserByUsername(user.getUsername()) == null) {
                    System.out.println("No result for indicated user. " + receiver + " doesn't exist.");
                    return filteredNotifications;
                }
                String command = "SELECT * FROM notifications WHERE receiver = ? ";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1, user.getUsername());
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        filteredNotifications.add(new Notification(
                                result.getString("sender"),
                                result.getString("receiver"),
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
                                result.getString("receiver"),
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

    public static List<Notification> getNotificationsByTimestamp(Object timestamp) {
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
                                result.getString("receiver"),
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

    public static User getReceiverByNotification(Notification notification) {
        String command = "SELECT userId FROM notifications WHERE id = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
             statement.execute("PRAGMA foreign_keys = ON;");
             p_statement.setInt(1, NotificationsDAO.getIdByNotification(notification));
            ResultSet result = p_statement.executeQuery();

            if(result.next()){
                return UserDAO.getUserByUsername(result.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getIdByNotification(Object notification) {
        try {
            if (notification instanceof Notification not) {
                String command = "SELECT id FROM notifications WHERE sender = and receiver = ? and timestamp = ?";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command); 
                     Statement statement = connection.createStatement()){
                     statement.execute("PRAGMA foreign_keys = ON;");
                     p_statement.setString(1,not.getSender());
                    p_statement.setString(2, not.getReceiver());
                    p_statement.setLong(3, not.getTimestamp());
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

    public static Notification getNotificationById(int id){
                String command = "SELECT * FROM notifications WHERE id = ? ";

                try (Connection connection = DatabaseManager.getDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command);
                     Statement statement = connection.createStatement()){
                    statement.execute("PRAGMA foreign_keys = ON;");
                    p_statement.setInt(1, id);
                    ResultSet result = p_statement.executeQuery();
                    if(result.next()){
                        return new Notification(
                                result.getString("sender"),
                                result.getString("receiver"),
                                result.getString("netId"),
                                result.getInt("type"),
                                result.getString("title"),
                                result.getString("text"),
                                result.getLong("timestamp"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        return null;
    }

    public static int getIdByNotification(Notification notification){
        String command = "SELECT id FROM notifications WHERE sender = ?, receiver = ?, timestamp = ? ";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON;");
            p_statement.setString(1, notification.getSender());
            p_statement.setString(2, notification.getReceiver());
            p_statement.setLong(3, notification.getTimestamp());
            ResultSet result = p_statement.executeQuery();
            if(result.next()){
                return result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Notification extractNotificationById(int id){
        String command = "SELECT * FROM notifications WHERE id = ? ";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command);
             Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON;");
            p_statement.setInt(1, id);
            ResultSet result = p_statement.executeQuery();
            if(result.next()){
                Notification not = new Notification(
                        result.getString("sender"),
                        result.getString("receiver"),
                        result.getString("netId"),
                        result.getInt("type"),
                        result.getString("title"),
                        result.getString("text"),
                        result.getLong("timestamp"));

                removeNotification(not);
                return not;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}