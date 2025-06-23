package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.database.UserDAO;
import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import static it.petrinet.utils.Validation.isValidInput;

public class LoginController {
    private static final String LOGO_PATH = "/assets/images/logo.png";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private ImageView logoView;
    @FXML private Button loginButton;

    @FXML
    // inizializza lo statuslabel (il messaggio di errore in caso di login fallito) e l'immagine del logo
    public void initialize() {
        // Hide error message initially
        statusLabel.setVisible(false);

        // Load logo
        loadLogoImage();

        // Enable Enter key to trigger login
        loginButton.setDefaultButton(true);

        // Check if there's a pending success message from registration
        String pendingMessage = ViewNavigator.getPendingMessage();
        if (pendingMessage != null) {
            showSuccess(pendingMessage);
            ViewNavigator.clearPendingMessage();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) throws InputTypeException{
        clearError();

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (!isValidInput(username, password)) {
            showError("Please enter username and password");
            return;
        }

        if (UserDAO.findSameUser(UserDAO.getUserByUsername(username), UserDAO.getUsersByPassword(password)) !=null){

            //Login successful
            User user = UserDAO.findSameUser(UserDAO.getUserByUsername(username), UserDAO.getUsersByPassword(password));
            ViewNavigator.setAuthenticatedUser(user);
            System.out.println("login successful for user: "+ user.getUsername());
            proceedToMainView(event, user);
        }
        else{
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

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setVisible(true);
    }

    private void clearError() {
        statusLabel.setVisible(false);
    }
}