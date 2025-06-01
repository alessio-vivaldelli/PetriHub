package it.petrinet.model.Database;

import it.petrinet.exceptions.ExceptionType;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import javax.swing.*;
import java.sql.*;

public class PetriNetsDAO implements DataAccessObject{
    public void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS petri_nets (" +
                "NetName TEXT NOT NULL, " +
                "XML_PATH TEXT NOT NULL, " +
                "creatorId INTEGER NOT NULL, " +
                "netId INTEGER NOT NULL UNIQUE, " +
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
        String command = "INSERT INTO petri_nets(NetName, XML_PATH, creatorId, netId, image_PATH, isReady ) VALUES (?, ?, ?, ?, ?, ?)";

        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setString(1, net.getNetName());
                    p_statement.setString(2, net.getXML_PATH());
                    p_statement.setString(3, net.getCreatorId());
                    p_statement.setInt(4, net.getNetId());
                    p_statement.setString(5, net.getImagePATH());
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

    public static void removeNet(Object p_net){
        String command = "DELETE FROM petri_nets WHERE netId = ?";
        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {
                    p_statement.setInt(1, net.getNetId());
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

    public static void setName(Object p_net, String newName){
        String command = "UPDATE petri_nets SET netName = ? WHERE idNet = ?";

        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setString(1, newName);
                    p_statement.setInt(2, net.getNetId());
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

    public static void setReady(Object p_net){
        String command = "UPDATE petri_nets SET isReady = ? WHERE netId = ?";

        try{
            if(p_net instanceof PetriNet net){
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)){
                    p_statement.setBoolean(1, true);
                    p_statement.setInt(2, net.getNetId());
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
                    p_statement.setInt(2, net.getNetId());
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

