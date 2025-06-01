package it.petrinet.model.Database;

public interface DataAccessObject {
    final String typeErrorMessage = "Object input has wrong type!";

    default void createTable(){} //di default è vuoto, così può essere usato dai DAO a piacimento
}
