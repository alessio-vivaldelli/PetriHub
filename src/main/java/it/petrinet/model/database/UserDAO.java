package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements DataAccessObject{
    public static void main(String args[]) throws InputTypeException {
//        removeUser(new User("davide", "sala", true));
//        removeUser(new User("Davide", "sala", false));
        insertUser(new User("Davide", "sala", true));

    }

    public static int getNumberOfOwnedNetsByUser(Object user) throws InputTypeException {
        String command = "SELECT COUNT(netName) FROM petri_nets WHERE creatorId = ?";
        int netsNumber = -1;
        try{
            if(user instanceof User u) {
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, u.getUsername());
                    ResultSet result = p_statement.executeQuery();

                    netsNumber = result.getInt(1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.USER);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return netsNumber;
    }

    public static int getNumberOfSubscribedNetsByUser(Object user) throws InputTypeException {
        int netsNumber = -1;
        try{
            if(user instanceof User u) {
                netsNumber = ComputationsDAO.getNetsSubscribedByUser(user).size();
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.USER);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return netsNumber;
    }


    public void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL, " +
                "isAdmin BOOLEAN NOT NULL)";

        try (Connection connection = DatabaseManager.getUserDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertUser(Object user) throws InputTypeException {                  //inserimento di un utente che si registra
        String command = "INSERT INTO users(username, password, isAdmin) VALUES (?, ?, ?)";

        try{
            if(user instanceof User u) {
                if (!DatabaseManager.tableExists("users", "users")) {
                    UserDAO dao = new UserDAO();
                    dao.createTable();
                }
                try (Connection connection = DatabaseManager.getUserDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, u.getUsername());
                    p_statement.setString(2, u.getPassword());
                    p_statement.setBoolean(3, u.isAdmin());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.USER);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static void modifyUserPassword(Object user, String password) throws InputTypeException {
        try{
            if (user instanceof User u) {
                String command = "UPDATE users SET password = ?, isAdmin = ? WHERE username = ?";

                try (Connection connection = DatabaseManager.getUserDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setString(3, u.getUsername());
                    p_statement.setString(1, password);
                    p_statement.setBoolean(2, u.isAdmin());
                    p_statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.USER);
            }
        }
        catch (InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static void removeUser(Object user){
        try{
            if (user instanceof User u) {
                String command = "DELETE FROM users WHERE username = ?";

                try (Connection connection = DatabaseManager.getUserDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setString(1, u.getUsername());
                    p_statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.USER);
            }
        }
        catch (InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static User getUserByUsername(String username) {
        String command = "SELECT * FROM users WHERE username = ? ";

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setString(1, username);
            ResultSet result = p_statement.executeQuery();

            if (result.next()) {
                 return new User(
                        username,
                        result.getString("password"),
                        result.getBoolean("isAdmin")
                 );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getUsersByPassword(String password) {
        String command = "SELECT * FROM users WHERE password = ? ";
        List <User> filteredUsers = new ArrayList<User>();

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setString(1, password);
            ResultSet result = p_statement.executeQuery();

            while(result.next()) {
                 filteredUsers.add(new User(
                        result.getString(1),
                        result.getString(2),
                        result.getBoolean(3)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filteredUsers;
    }

    public static User findSameUser(Object user, List <User> group2) {
        try{
            if(user instanceof User u){
                for(User user2 : group2){
                    if (u.equals(user2)){
                        return user2;
                    }
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.USER);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return null;
    }
}
