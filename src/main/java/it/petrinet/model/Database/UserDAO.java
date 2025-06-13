package it.petrinet.model.Database;

import it.petrinet.exceptions.ExceptionType;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import it.petrinet.model.Database.DatabaseManager.*;
import javafx.scene.chart.PieChart;

public class UserDAO implements DataAccessObject{

//    public static void main(String[] args) throws InputTypeException {
//        User davide = new User("davide", "davide", false);
//        modifyUserPassword(davide, "sala");
//    }
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
                    p_statement.setInt(3, u.isAdmin() ? 1 : 0);
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.USER);
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
                    connection.commit();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.USER);
            }
        }
        catch (InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static void deleteUser(String username){
        String command = "DELETE FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.getUserDBConnection();
            PreparedStatement p_statement = connection.prepareStatement(command)){
                p_statement.setString(1, username);
                p_statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static User getUserByUsername(String username) {
        String search_command = "SELECT * FROM users WHERE username = ? ";

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
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
        String search_command = "SELECT * FROM users WHERE password = ? ";
        List <User> filteredUsers = new ArrayList<User>();

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
            p_statement.setString(1, password);
            ResultSet result = p_statement.executeQuery();

            while(result.next()) {
                 filteredUsers.add(new User(
                        result.getString("username"),
                        password,
                        result.getBoolean("isAdmin")
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
                throw new InputTypeException(typeErrorMessage,ExceptionType.USER);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return null;
    }
}
