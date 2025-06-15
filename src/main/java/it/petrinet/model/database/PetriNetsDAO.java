package it.petrinet.model.database;

import it.petrinet.exceptions.ExceptionType;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.PetriNet;

import java.sql.*;
import java.util.jar.Manifest;

public class PetriNetsDAO implements DataAccessObject{

    public void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS petri_nets (" +
                "netName TEXT PRIMARY KEY, " +
                "creatorId INTEGER NOT NULL, " +
                "creationDate INTEGER NOT NULL, " +
                "XML_PATH TEXT NOT NULL, " +
                "image_PATH TEXT NOT NULL, " +
                "isReady BOOLEAN NOT NULL)";

        try (Connection conn = DatabaseManager.getPetriNetsDBConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertNet(Object p_net) throws InputTypeException{                //c'è da rimpiazzare i placeholder coi get della classe delle reti
        String command = "INSERT INTO petri_nets(netName, creatorId, creationDate, XML_PATH, image_PATH, isReady ) VALUES (?, ?, ?, ?, ?, ?)";
        try{
            if(!DatabaseManager.tableExists("nets", "petri_nets")){
                PetriNetsDAO dao = new PetriNetsDAO();
                dao.createTable();
            }
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, net.getNetName());
                    p_statement.setString(2, net.getCreatorId());
                    p_statement.setString(3, net.getXML_PATH());
                    p_statement.setInt(4,net.getCreationDate());
                    p_statement.setString(5, net.getImage_PATH());
                    p_statement.setBoolean(6, net.isReady());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.PETRI_NET);
            }
        }
        catch(InputTypeException e) {
            e.ErrorPrinter();
        }
    }

    public static void removeNet(Object p_net) throws InputTypeException{
        String command = "DELETE FROM petri_nets WHERE netName = ?";
        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, net.getNetName());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.PETRI_NET);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
    }

    public static void setReady(Object p_net) throws InputTypeException{
        String command = "UPDATE petri_nets SET isReady = ? WHERE netName = ?";

        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setBoolean(1, true);
                    p_statement.setString(2, net.getNetName());
                    p_statement.executeUpdate();
                }
                catch (SQLException e){
                    e.printStackTrace();
                }
            }
            else{
                throw new InputTypeException(typeErrorMessage, ExceptionType.PETRI_NET);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }

    }

    public static void changeImage(Object p_net, String newDir) throws InputTypeException{
        String command = "UPDATE petri_nets SET image_PATH = ? WHERE netName = ?";
        try {
            if (p_net instanceof PetriNet net) {
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, newDir);
                    p_statement.setString(2, net.getNetName());
                    p_statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, ExceptionType.PETRI_NET);
            }
        }
        catch(InputTypeException e){
                e.ErrorPrinter();
        }
    }
}

