package it.petrinet.controller;

import static it.petrinet.Main.isValidInput;
import it.petrinet.controller.LoginController;

import it.petrinet.Main;
import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView logoView;

    private DB newDB = new DB(); // Da cancellare dopo aver fatto db

    @FXML
    public void initialize() { // guarda il duale LoginController
        statusLabel.setVisible(false);
        Image img = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
        logoView.setImage(img);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (!isValidInput(username, password)) {
            showError("Please fill out all fields");
            return;
        }

        if (newDB.getUsers().stream().anyMatch(x -> x.getUsername().equals(username))) { // Da cambiare con il DB
            showError("Username already exists");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Create and save new user
        User newUser = User.create(username, password);

        newDB.addUser(newUser);

        // Show success message
        showSuccess("Registration successful! Please log in.");

        // Clear fields
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleBackToLogin() {
        ViewNavigator.navigateToLogin();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setVisible(true);
    }
}