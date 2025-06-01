package it.petrinet.model.Database;

import it.petrinet.model.User;

import java.sql.*;

public class PetriNetsDAO {
    public static void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS petri_nets (" +
                "NetName TEXT NOT NULL, " +
                "XML_PATH TEXT NOT NULL, " +
                "creatorId INTEGER NOT NULL, " +
                "netId INTEGER NOT NULL UNIQUE, " +
                "image_PATH TEXT NOT NULL, " +
                "isReady BOOLEAN NOT NULL)";

        try (Connection conn = DatabaseManager.getPetriNetsDBConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void addNet(Object net) {                //c'è da rimpiazzare i placeholder coi get della classe delle reti
        String command = "INSERT INTO petri_nets(NetName, XML_PATH, creatorId, netId, image_PATH, isReady ) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setString(1, "name");
            p_statement.setString(2, "path");
            p_statement.setInt(1, 0);
            p_statement.setInt(1, 1 );
            p_statement.setString(1, "imagepath");
            p_statement.setBoolean(4, true);
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void removeNet(Object net) {                //rimpiazza placeholder
        String command = "DELETE FROM petri_nets WHERE netId = ?";

        try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setInt(1, 0);
            p_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setName(Object net){
        String command = "UPDATE petri_nets SET netName = ? WHERE idNet = ?";
        try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)){
            p_statement.setString(1, "Pappa");
            p_statement.setInt(2, 939939393);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void setReady(Object net){
        String command = "UPDATE petri_nets SET isReady = ? WHERE netId = ?";

        try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
             PreparedStatement p_statement = connection.prepareStatement(command)){
            p_statement.setBoolean(1, true);
            p_statement.setInt(2, 53550000);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void changeImage(Object net, String dir){
        //if (net instanceof petriNet pn){
        String command = "UPDATE petri_nets SET image_PATH = ? WHERE netId = ?";
        try (Connection connection = DatabaseManager.getUserDBConnection();
        PreparedStatement p_statement = connection.prepareStatement(command)) {
            p_statement.setString(1, dir);
            p_statement.setInt(2, 4);
            ResultSet result = p_statement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
