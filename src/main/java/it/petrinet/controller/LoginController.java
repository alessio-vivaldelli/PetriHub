package it.petrinet.controller;

import it.petrinet.model.DB;
import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import static it.petrinet.utils.Validation.isValidInput;

public class LoginController {
    private static final String LOGO_PATH      = "/assets/images/logo.png";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private ImageView logoView;
    @FXML private Button loginButton;

    private final DB database = new DB(); // TODO: replace with DI

    @FXML
    public void initialize() {
        // Hide error message initially
        statusLabel.setVisible(false);

        // Load logo
        loadLogoImage();

        // Enable Enter key to trigger login
        loginButton.setDefaultButton(true);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        clearError();

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (!isValidInput(username, password)) {
            showError("Please enter username and password");
            return;
        }

        Optional<User> userOpt = database.getUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();

        if (userOpt.isPresent() && userOpt.get().checkPassword(password)) {
            proceedToMainView(event, userOpt.get());
        } else {
            showError("Invalid username or password");
        }

    }

    @FXML
    private void handleRegister() {
        ViewNavigator.navigateToRegister();
    }

    private void loadLogoImage() {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(LOGO_PATH)));
            logoView.setImage(img);
        } catch (Exception e) {
            System.err.println("Unable to load logo: " + LOGO_PATH);
        }
    }

    private void proceedToMainView(ActionEvent event, User user) {
        ViewNavigator.setAuthenticatedUser(user);
        ViewNavigator.HomeScene();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setVisible(true);
    }

    private void clearError() {
        statusLabel.setVisible(false);
    }
}
