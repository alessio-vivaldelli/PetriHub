package it.petrinet.model.database;

import java.sql.*;

public class DatabaseManager {
    private static final String globalDatabaseDir = "jdbc:sqlite:src/main/resources/database/database.db";
    private static final String globalDir = "jdbc:sqlite:src/main/resources/database/";


    public static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(globalDatabaseDir);
    }

    public static String getGlobalDBDir(){
        return globalDatabaseDir;
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
    public static void enableForeignKeys(Statement statement) throws SQLException {
        statement.execute("PRAGMA foreign_keys = ON;");
    }

    /**
     * Centralized SQLException handler.
     *
     * @param methodName the name of the method where the exception occurred
     * @param ex the SQLException thrown
     */
    public static void handleSQLException(String methodName, SQLException ex) {
        System.err.println("SQLException in " + methodName + ": " + ex.getMessage());
        ex.printStackTrace();
    }
}