package it.petrinet.model.Database;

import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import it.petrinet.model.Database.DatabaseManager.*;
import javafx.scene.chart.PieChart;

public class UserDAO {

    public static void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "isAdmin BOOLEAN NOT NULL)";

        try (Connection conn = DatabaseManager.getUserDBConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertUser(User user) {                  //inserimento di un utente che si registra
        String command = "INSERT INTO users(id, username, password, isAdmin) VALUES (?, ?, ?, ?)";

        if (!DatabaseManager.tableExists("users", "users")){
            createTable();
        }

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setInt(1, user.getId());
            p_statement.setString(2, user.getUsername());
            p_statement.setString(3, user.getPassword());
            p_statement.setInt(4, user.isAdmin() ? 1 : 0);
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void modifyUser(Object user, int id, String table) {                  //inserimento di un utente che si registra
        if (user instanceof User u) {
            User oldUser = getUserById(id);
            assert oldUser != null;

            StringBuilder command = new StringBuilder("UPDATE ");
            String del_command = "UPDATE " + table + " SET username = ?, password = ? isAdmin = ? WHERE id = ?";

            try (Connection connection = DatabaseManager.getUserDBConnection();
                 PreparedStatement p_statement = connection.prepareStatement((command.toString()))){
                        p_statement.setString(1, u.getUsername());
                        p_statement.setString(2, u.getPassword());
                        p_statement.setBoolean(3, u.isAdmin());
                        p_statement.setInt(4, u.getId());
                        p_statement.executeUpdate();
                    }
            catch (SQLException e) {
                e.printStackTrace();
                }
        }
    }

    public static void deleteUser(int id, String table){
        String command = "DELETE FROM " + table + " WHERE id = ?";

        try (Connection connection = DatabaseManager.getUserDBConnection();
            PreparedStatement p_statement = connection.prepareStatement(command)){
                p_statement.setInt(1, id);
                p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static User getUserById(int id) {
        String search_command = "SELECT * FROM users WHERE id = " + id;

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
            p_statement.setInt(0, id);
            ResultSet result = p_statement.executeQuery();

            if (result.next()) {
                return new User(
                        id,
                        result.getString("username"),
                        result.getString("password"),
                        result.getBoolean("isAdmin")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getUsersByUsername(String username) {       //TODO non so se posso tenerlo non statico
        String search_command = "SELECT * FROM users WHERE username = ";
        List <User> filteredUsers = new ArrayList<User>();

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
            p_statement.setString(1, username);
            ResultSet result = p_statement.executeQuery();


            while (result.next()) {
                 filteredUsers.add(new User(
                        result.getInt("id"),
                        username,
                        result.getString("password"),
                        result.getBoolean("isAdmin")
                 ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filteredUsers;
    }
    public static List<User> getUsersByPassword(String password) {
        String search_command = "SELECT * FROM users WHERE password = ";
        List <User> filteredUsers = new ArrayList<User>();

        try (Connection connection = DatabaseManager.getUserDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
            p_statement.setString(2, password);
            ResultSet result = p_statement.executeQuery();

            while(result.next()) {
                 filteredUsers.add(new User(
                        result.getInt("id"),
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

    public static User findSameUser(List<User> group1, List <User> group2){
        for(User user1 : group1){
            for(User user2 : group2){
                if (user1.equals(user2)){
                    return user1;
                }
            }
        }
        return null;
    }

}
