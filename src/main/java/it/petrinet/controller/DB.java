package it.petrinet.controller;

import it.petrinet.model.User;

import java.util.List;

public class DB {

    private List<User> users = List.of(
            User.create("admin", "admin", true),
            User.create("user", "user")
    ); // Da cancellare dopo aver fatto db
    //--------------------------------------

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        List<User> now = getUsers();
        now.add(user);
        users = now; // ci ho provato
    }
}
