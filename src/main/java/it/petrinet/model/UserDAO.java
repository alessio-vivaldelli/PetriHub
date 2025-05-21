package it.petrinet.model;

import java.sql.*;

public class UserDAO {
    public static void createTable() {                          //metodo per creazione tabelle
        String table = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "isAdmin INTEGER NOT NULL)";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertUser(User user) {                  //inserimento di un utente che si registra
        String sql = "INSERT INTO users(id, username, password, isAdmin) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement p_statement = connection.prepareStatement(sql)) {
            p_statement.setInt(1, user.getId());
            p_statement.setString(2, user.getUsername());
            p_statement.setString(3, user.getPassword());
            p_statement.setInt(4, user.isAdmin() ? 1 : 0);
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static User getUserById(String id) {
        String search_command = "SELECT * FROM users WHERE id = ";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
            p_statement.setString(0, id);
            ResultSet result = p_statement.executeQuery();
            if (result.next()) {
                return new User(
                        result.getString("username"),
                        result.getString("password"),
                        result.getInt("isAdmin") == 1
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserByUsername(String username) {
        String search_command = "SELECT * FROM users WHERE username = ";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
            p_statement.setString(1, username);
            ResultSet result = p_statement.executeQuery();
            if (result.next()) {
                return new User(
                        result.getString("id"),
                        result.getString("password"),
                        result.getInt("isAdmin") == 1
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static User getUserByPassword(String password) {
        String search_command = "SELECT * FROM users WHERE password = ";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement p_statement = connection.prepareStatement(search_command)) {
            p_statement.setString(2, password);
            ResultSet result = p_statement.executeQuery();
            if (result.next()) {
                return new User(
                        result.getString("id"),
                        result.getString("password"),
                        result.getInt("isAdmin") == 1
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
