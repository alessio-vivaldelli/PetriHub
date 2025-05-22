package it.petrinet.view.components;

import it.petrinet.view.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class NavBar extends HBox {

    private final Button homeButton = createNavButton("🏠 Home", ViewNavigator::navigateToHome);
    private final Button projectsButton = createNavButton("📄 Projects", this::handleProjects);
    private final Button logoutButton = createNavButton("🚪 Logout", this::handleLogout);

    public NavBar() {
        setupLayout();
        this.getChildren().addAll(homeButton, projectsButton, logoutButton);
    }

    /** Setup stile e padding */
    private void setupLayout() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: #181825;");
    }

    /** Crea un bottone con stile e azione */
    private static Button createNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #1e1e2e;" +
                        "-fx-text-fill: #cdd6f4;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-border-color: #b4befe;" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnAction(e -> action.run());
        return button;
    }

    /** Placeholder per progetti */
    private void handleProjects() {
        System.out.println("Progetti non ancora implementati");
        // ViewNavigator.navigateToProjects();
    }

    /** Azione di logout */
    private void handleLogout() {
        // reset utente autenticato
        ViewNavigator.setAuthenticatedUser(null);
        ViewNavigator.LoginScene();
    }
}
