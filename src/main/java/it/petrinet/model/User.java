package it.petrinet.model;

public class User {
    private int id; // Unique ID for the user
    private String username;
    private String password;
    private boolean isAdmin;

    public User(int id, String username, String password, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public static User create(String username, String password, boolean isAdmin) {
        return new User(username. hashCode(), username, password, isAdmin);
    }

    public static User create(String username, String password) {
        return new User(username.hashCode(), username, password, false);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isAdmin() { return isAdmin; }

    public boolean equals(User other) {
        return this.id == other.getId();
    }
}
