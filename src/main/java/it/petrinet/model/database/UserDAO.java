package it.petrinet.model.database;

import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing User entities in the database.
 */
public class UserDAO implements DataAccessObject {

    /**
     * Creates the 'users' table in the database if it does not exist.
     */
    public void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL, " +
                "isAdmin BOOLEAN NOT NULL)";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
        DatabaseManager.enableForeignKeys(statement);
            statement.execute(createTableSQL);
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("createUsersTable", ex);
        }
    }

    /**
     * Deletes the 'users' table from the database.
     */
    public void deleteTable() {
        String dropSQL = "DROP TABLE users;";

        try (Connection connection = DatabaseManager.getDBConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(dropSQL);
        } catch (SQLException ex) {
            DatabaseManager.handleSQLException("deleteTable", ex);
        }
    }

    /**
     * Inserts a new user into the database.
     *
     * @param user the User object to insert
     */
    public static void insertUser(User user) {
        String insertSQL = "INSERT INTO users(username, password, isAdmin) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement pstmt = connection.prepareStatement(insertSQL);
             Statement statement = connection.createStatement()) {

            if (!DatabaseManager.tableExists("users")) {
                new UserDAO().createTable();
            }

            DatabaseManager.enableForeignKeys(statement);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setBoolean(3, user.isAdmin());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("insertUser", e);
        }
    }

    /**
     * Updates a user's password and admin status.
     *
     * @param user     the user to modify
     * @param password the new password
     */
    public static void modifyUserPassword(User user, String password) {
        String updateSQL = "UPDATE users SET password = ?, isAdmin = ? WHERE username = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement pstmt = connection.prepareStatement(updateSQL);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);
            pstmt.setString(1, password);
            pstmt.setBoolean(2, user.isAdmin());
            pstmt.setString(3, user.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("ModifyUserPassword", e);
        }
    }

    /**
     * Removes a user from the database.
     *
     * @param user the User to remove
     */
    public static void removeUser(User user) {
        String deleteSQL = "DELETE FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement pstmt = connection.prepareStatement(deleteSQL);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);
            pstmt.setString(1, user.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("removeUser", e);
        }
    }

    /**
     * Retrieves a user by username.
     *
     * @param username the username to search for
     * @return the User object if found, otherwise null
     */
    public static User getUserByUsername(String username) {
        String querySQL = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement pstmt = connection.prepareStatement(querySQL);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);
            pstmt.setString(1, username);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                return new User(
                        result.getString("username"),
                        result.getString("password"),
                        result.getBoolean("isAdmin")
                );
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getUserByUsername", e);
        }
        return null;
    }

    /**
     * Retrieves all users with the given password.
     *
     * @param password the password to filter by
     * @return a list of users with the given password
     */
    public static List<User> getUsersByPassword(String password) {
        String querySQL = "SELECT * FROM users WHERE password = ?";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseManager.getDBConnection();
             PreparedStatement pstmt = connection.prepareStatement(querySQL);
             Statement statement = connection.createStatement()) {

            DatabaseManager.enableForeignKeys(statement);
            pstmt.setString(1, password);
            ResultSet result = pstmt.executeQuery();

            while (result.next()) {
                users.add(new User(
                        result.getString("username"),
                        result.getString("password"),
                        result.getBoolean("isAdmin")
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.handleSQLException("getUsersbyPassword", e);
        }
        return users;
    }

    /**
     * Finds and returns the same user in a different list.
     *
     * @param user   the user to look for
     * @param users  the list to search in
     * @return the matching user if found, otherwise null
     */
    public static User findSameUser(User user, List<User> users) {
        for (User u : users) {
            if (user.equals(u)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Gets the number of Petri nets created by a user.
     *
     * @param user the user
     * @return number of owned nets
     */
    public static int getNumberOfOwnedNetsByUser(User user) {
        return PetriNetsDAO.getNetsByCreator(user).size();
    }

    /**
     * Gets the number of Petri nets a user is subscribed to.
     *
     * @param user the user
     * @return number of subscribed nets
     */
    public static int getNumberOfSubscribedNetsByUser(User user) {
        return ComputationsDAO.getNetsSubscribedByUser(user).size();
    }
}
