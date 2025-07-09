package it.petrinet.controller;

import it.petrinet.model.User;
import it.petrinet.model.database.UserDAO;
import it.petrinet.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import static it.petrinet.utils.Safenavigate.safeNavigate;
import static it.petrinet.utils.Validation.isValidInput;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label statusLabel;

    @FXML private ImageView logoView;

    @FXML
    public void initialize() { // guarda il duale LoginController
        statusLabel.setVisible(false);
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/logo.png")));
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

        if(UserDAO.getUserByUsername(username) != null){
            showError("Username already exists");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Create and save new user
        User newUser = User.create(username, password);

        UserDAO.insertUser(newUser);

        // Navigate to login with success message
        safeNavigate(() -> ViewNavigator.toLoginWithMessage("Registration successful!"));
    }

    @FXML
    private void handleBackToLogin() {
        ViewNavigator.toLogin();
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