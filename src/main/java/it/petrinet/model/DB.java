package it.petrinet.model;

import java.util.List;

public class DB {

    private final List<User> users = List.of(
            User.create("admin", "admin", true),
            User.create("user", "user"),
            User.create("a", "a", true),
            User.create("b", "b", false)
    ); // Da cancellare dopo aver fatto db
    //--------------------------------------

    public List<User> getUsers() {
        return users;
    }

    private static final List<String> nets = List.of("net1", "net2" , "net3", "net4");

    public static List<String> getNets() {
        return nets;
    }

}
