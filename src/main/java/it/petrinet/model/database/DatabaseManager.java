package it.petrinet.model.database;

import java.sql.*;

public class DatabaseManager {
    private static final String USER_DB_URL = "jdbc:sqlite:users.db";
    private static final String PETRI_NETS_DB_URL = "jdbc:sqlite:nets.db";
    private static final String NOTIFICATIONS_DB_URL = "jdbc:sqlite:notifications.db";
    private static final String COMPUTATIONS_DB_URL = "jdbc:sqlite:computations.db";
//    private static final String COMPUTATION_STEPS_DB_URL = "jdbc:sqlite:steps.db";

    public static Connection getUserDBConnection() throws SQLException {
        return DriverManager.getConnection(USER_DB_URL);
    }

    public static Connection getPetriNetsDBConnection() throws SQLException {
        return DriverManager.getConnection(PETRI_NETS_DB_URL);
    }

    public static Connection getNotificationsDBConnection() throws SQLException {
        return DriverManager.getConnection(NOTIFICATIONS_DB_URL);
    }

    public static Connection getComputationsDBConnection() throws SQLException{
        return DriverManager.getConnection(COMPUTATIONS_DB_URL);
    }

//    public static Connection getComputationStepsDBConnection() throws SQLException{
//        return DriverManager.getConnection(COMPUTATION_STEPS_DB_URL);
//    }

    protected static boolean tableExists(String file, String name) {
        String db_URL = "jdbc:sqlite:" + file + ".db";
        if(!db_URL.equals(USER_DB_URL) & !db_URL.equals(PETRI_NETS_DB_URL)){
            System.err.println("Unfindable database");
            return true;
        }

        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(db_URL);
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getTables(null, null, name.toUpperCase(), null);
            return resultSet.next();
        }
        catch (SQLException e) {
            System.err.println("Table Data gathering returned error:" + e.getMessage());
            return false;
        }
        finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Table Data closing returned error: " + e.getMessage());
            }
        }
    }
}