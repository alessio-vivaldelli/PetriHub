package it.petrinet.model.database;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.PetriNet;
import it.petrinet.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PetriNetsDAO implements DataAccessObject{
    public static void main(String[] args) throws InputTypeException{
        System.out.println(getUnknownNetsByUser(new User("Davide", "sala", false)));
        //insertNet(new PetriNet("net2", "ale", 101, "XML", "image", true ));
    }


    public void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS petri_nets (" +
                "netName TEXT PRIMARY KEY, " +
                "creatorId TEXT NOT NULL, " +
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.PETRI_NET);
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.PETRI_NET);
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.PETRI_NET);
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
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.PETRI_NET);
            }
        }
        catch(InputTypeException e){
                e.ErrorPrinter();
        }
    }

    public static List<PetriNet> getUnknownNetsByUser(Object user) throws InputTypeException{
        List<PetriNet> wantedNets = new ArrayList<PetriNet>();
        String command = "SELECT * FROM petri_nets WHERE creatorId <> ?";

        try {
            if (user instanceof User u) {
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_statement = connection.prepareStatement(command)) {

                    p_statement.setString(1, u.getUsername());
                    ResultSet result = p_statement.executeQuery();
                    while(result.next()){
                        wantedNets.add(new PetriNet(
                                result.getString(1),
                                result.getString(2),
                                result.getInt(3),
                                result.getString(4),
                                result.getString(5),
                                result.getBoolean(6)
                        ));
                    }

                    List<PetriNet> toRemove = ComputationsDAO.getNetsSubscribedByUser(u);
                    for(PetriNet removableNet : toRemove){
                        wantedNets.removeIf(pNet -> removableNet.getNetName().equals(pNet.getNetName()));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.PETRI_NET);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedNets;
    }

    public static List<PetriNet> getNetsByCreator(Object admin) throws InputTypeException{
        List<PetriNet> wantedNets = new ArrayList<PetriNet>();
        try{
            if(admin instanceof User u){
                String command = "SELECT * FROM petri_nets WHERE creatorId = ?";
                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, u.getUsername());
                    ResultSet result = p_Statement.executeQuery();
                    while(result.next()){
                        wantedNets.add( new PetriNet(
                                result.getString(1),
                                result.getString(2),
                                result.getInt(3),
                                result.getString(4),
                                result.getString(5),
                                result.getBoolean(6)
                        ));
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return wantedNets;
    }

    public static PetriNet getNetByName(Object name) throws InputTypeException{
        try{
            if(name instanceof String n){
                String command = "SELECT * FROM petri_nets WHERE netName = ?";

                try (Connection connection = DatabaseManager.getPetriNetsDBConnection();
                     PreparedStatement p_Statement = connection.prepareStatement(command)){
                    p_Statement.setString(1, n);
                    ResultSet result = p_Statement.executeQuery();
                    if(result.next()){
                        return new PetriNet(
                                result.getString(1),
                                result.getString(2),
                                result.getInt(3),
                                result.getString(4),
                                result.getString(5),
                                result.getBoolean(6)
                        );
                    }
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            else {
                throw new InputTypeException(typeErrorMessage, InputTypeException.ExceptionType.COMPUTATION);
            }
        }
        catch(InputTypeException e){
            e.ErrorPrinter();
        }
        return null;
    }

}

