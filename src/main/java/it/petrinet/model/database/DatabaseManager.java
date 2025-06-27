package it.petrinet.model.database;

import java.sql.*;

public class DatabaseManager {
    private static final String globalDir = "jdbc:sqlite:src/main/resources/database/";

    private static final String USER_DB_URL =  globalDir + "users.db";
    private static final String PETRI_NETS_DB_URL =  globalDir + "nets.db";
    private static final String NOTIFICATIONS_DB_URL = globalDir + "notifications.db";
    public static Connection getUserDBConnection() throws SQLException {
        return DriverManager.getConnection(USER_DB_URL);
    }

    public static Connection getPetriNetsDBConnection() throws SQLException {
        return DriverManager.getConnection(PETRI_NETS_DB_URL);
    }

    public static Connection getNotificationsDBConnection() throws SQLException {
        return DriverManager.getConnection(NOTIFICATIONS_DB_URL);
    }

    public static String getGlobalDir(){
        return globalDir;
    }

    public static String getNotificationsDbUrl() {
        return NOTIFICATIONS_DB_URL;
    }

    public static String getPetriNetsDbUrl() {
        return PETRI_NETS_DB_URL;
    }

    public static String getUserDbUrl() {
        return USER_DB_URL;
    }

    public static boolean tableExists(String dbName, String tableName) throws SQLException {
        boolean exists = false;
        String db_URL = globalDir + dbName + ".db";
        try (Connection connection = DriverManager.getConnection(db_URL)) {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet tables = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (tables.next()) {
                    exists = true;
                }
            }
        }
        return exists;
    }
}