package it.petrinet.model.database;

public interface DataAccessObject {

    default void createTable(){} //di default è vuoto, così può essere usato dai DAO a piacimento
    default void deleteTable(){}
}
