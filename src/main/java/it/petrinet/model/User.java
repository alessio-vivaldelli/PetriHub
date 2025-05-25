package it.petrinet.model;

public class User {
    //simil user, lo fatto solo per vedere se funziona il login, ma se volete lasciamo cosi, magari aggiungendo una lista di pertinet (my sub o roba simile)

    private int id; // Unique ID for the user
    private String username;
    private String password;
    private boolean isAdmin;

    public User(String username, String password, boolean isAdmin) {
        this.id = username.hashCode();
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public static User create(String username, String password, boolean isAdmin) {
        return new User(username, password, isAdmin);
    }

    public static User create(String username, String password) {
        return new User(username, password, false);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isAdmin() { return isAdmin; }

    public boolean hasCreation(){return isAdmin;}

    public boolean hasSubs() {return true;}

    public boolean hasDiscovery(){return true;}
}
