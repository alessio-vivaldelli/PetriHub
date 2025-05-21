package it.petrinet.controller;


import static it.petrinet.Main.isValidInput;

import it.petrinet.Main;
import it.petrinet.Main.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView logoView;

    private DB newDB = new DB(); // Da cancellare dopo aver fatto db

    @FXML
    // inizializza lo statuslabel (il messaggio di errore in caso di login fallito) e l'immagine del logo
    public void initialize() {
        statusLabel.setVisible(false);
        Image img = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
        logoView.setImage(img);
    }

    @FXML
    private void handleLogin() { //pulsante che accetta il login nel caso andasse bene (passa al model che poi rigira al view la schemrata giusta (credo)
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (!isValidInput(username, password)) {
            showError("Please enter username and password");
            return;
        }

        User user = newDB.getUsers().stream()
                .filter(u -> u.getUsername().equals(username))  //Sto provandoooooo!!!!
                .findFirst()
                .orElse(null);


        if (user != null && user.checkPassword(password)) {
            // Login successful
            ViewNavigator.setAuthenticatedUser(user);
            System.out.println("Login successful for user: " + user.getUsername());
        } else {
            showError("Invalid username or password");
        }
    }

    @FXML
    private void handleRegister() { //easy peasy switch di scena
        ViewNavigator.navigateToRegister();
    }

    private void showError(String message) { //errore di login
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setVisible(true);
    }
}