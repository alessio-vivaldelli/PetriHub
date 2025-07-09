package it.petrinet.model.database;

import java.sql.*;

public class DatabaseManager {
    private static final String globalDatabaseDir = "jdbc:sqlite:src/main/resources/database/database.db";
    private static final String globalDir = "jdbc:sqlite:src/main/resources/database/";

//    private static final String USER_DB_URL =  globalDir + "users.db";
//    private static final String PETRI_NETS_DB_URL =  globalDir + "nets.db";
//    private static final String NOTIFICATIONS_DB_URL = globalDir + "notifications.db";

    public static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(globalDatabaseDir);
    }

    public static String getGlobalDBDir(){
        return globalDatabaseDir;
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
    public static boolean tableExists(String tableName) throws SQLException {
        boolean exists = false;
        try (Connection connection = DriverManager.getConnection(getGlobalDBDir())) {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet tables = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (tables.next()) {
                    exists = true;
                }
            }
        }
        return exists;
    }

    /**
     * Enables foreign key support in SQLite.
     *
     * @param statement the statement on which to enable foreign keys
     * @throws SQLException if SQL execution fails
     */
    protected static void enableForeignKeys(Statement statement) throws SQLException {
        statement.execute("PRAGMA foreign_keys = ON;");
    }
}