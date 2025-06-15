package it.petrinet.model.database;

import it.petrinet.exceptions.ExceptionType;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.PetriNet;

import java.sql.*;

public class PetriNetsDAO implements DataAccessObject{

    public static void main(String[] args){

    }
    public void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS petri_nets (" +
                "netName TEXT NOT NULL, " +
                "XML_PATH TEXT NOT NULL, " +
                "creatorId INTEGER NOT NULL, " +
                "creationDate INTEGER NOT NULL, " +
                "image_PATH TEXT NOT NULL, " +
                "isReady BOOLEAN NOT NULL)";

        try (Connection conn = DatabaseManager.getPetriNetsDBConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate(table);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void addNet(Object p_net) {                //c'è da rimpiazzare i placeholder coi get della classe delle reti
        String command = "INSERT INTO petri_nets(netName, XML_PATH, creatorId, creationDatem image_PATH, isReady ) VALUES (?, ?, ?, ?, ?, ?)";

        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, net.getNetName());
                    p_statement.setString(2, net.getXML_PATH());
                    p_statement.setString(3, net.getCreatorId());
                    p_statement.setInt(5,net.getCreationDate());
                    p_statement.setString(6, net.getImagePATH());
                    p_statement.setBoolean(7, net.isReady());
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

    public static void removeNet(Object p_net){
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

//    public static void setName(Object p_net, String newName){
//        String command = "UPDATE petri_nets SET netName = ? WHERE netName = ?";
//
//        try{
//            if(p_net instanceof PetriNet net){
//                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
//                     PreparedStatement p_statement = connection.prepareStatement(command)){
//                    p_statement.setString(1, newName);
//                    p_statement.setString(2, net.getNetName());
//                }
//                catch (SQLException e){
//                    e.printStackTrace();
//                }
//            }
//            else{
//                throw new InputTypeException(typeErrorMessage, ExceptionType.PETRI_NET);
//            }
//        }
//        catch(InputTypeException e){
//            e.ErrorPrinter();
//        }
//
//    }

    public static void setReady(Object p_net){
        String command = "UPDATE petri_nets SET isReady = ? WHERE netName = ?";

        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setBoolean(1, true);
                    p_statement.setString(2, net.getNetName());
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

    public static void changeImage(Object p_net, String dir){
        try {
            if (p_net instanceof PetriNet net) {
                String command = "UPDATE petri_nets SET image_PATH = ? WHERE netId = ?";
                try (Connection connection = DatabaseManager.getUserDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, dir);
                    p_statement.setString(2, net.getNetName());
                    p_statement.executeQuery();

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

