package it.petrinet.view;

import it.petrinet.Main;
import it.petrinet.controller.*;
import it.petrinet.model.PetriNet;
import it.petrinet.model.Computation;
import it.petrinet.controller.NetVisualController.VisualState;
import it.petrinet.model.TableRow.NetCategory;
import it.petrinet.view.components.NavBar;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

public final class ViewNavigator {
    private static MainController mainController;

    private ViewNavigator() { }

    public static void init(MainController controller) {
        mainController = Objects.requireNonNull(controller, "MainController cannot be null");
    }

    public static void loginScene(boolean resize) {
        mainController.setNavBar(null);
        if(resize) resizeStage(500, 400, "PH - Login");
        loadView("LoginView.fxml");
    }

    public static void homeScene(boolean resize) {
        mainController.setNavBar(new NavBar());
        if(resize) resizeStage(0, 0, "PH - Home");
        loadView("HomeView.fxml");
    }

    public static void toLogin() { loadView("LoginView.fxml"); }
    public static void toHome() { loadView("HomeView.fxml"); }
    public static void toRegister() { loadView("RegisterView.fxml"); }
    public static void toMyNets()    { navigateToShowAll(NetCategory.OWNED); }
    public static void toSubscribedNets() { navigateToShowAll(NetCategory.SUBSCRIBED); }
    public static void toDiscoverNets() { navigateToShowAll(NetCategory.DISCOVER); }
    public static void toComputationsList(String netId){
        loadView("ComputationListView.fxml", ComputationListController.class,
                controller -> controller.initData(netId));
    }
    public static void toNetVisual(PetriNet m, Computation d, VisualState s) {
        mainController.setNavBar(null);
        loadView("NetVisualView.fxml", NetVisualController.class,
                c -> c.initData(m, d, s));
    }
    public static void toNetCreation(String netName) {
        mainController.setNavBar(null);
        loadView("NetCreationView.fxml", NetCreationController.class,
                controller -> controller.initData(netName));
    }




    // ... other navigation methods ...

    private static void navigateToShowAll(NetCategory type) {
        loadView("ShowAllView.fxml", ShowAllController.class,
                c -> c.initData(type));
    }

    private static <T> void loadView(String fxml, Class<T> cls, Consumer<T> init) {
        if (mainController == null)
            throw new IllegalStateException("Call init() first.");

        try {
            URL url = Main.class.getResource("/fxml/" + fxml);
            if (url == null) throw new IOException("Missing: " + fxml);
            FXMLLoader loader = new FXMLLoader(url);
            Pane view = loader.load();
            T controller = loader.getController();

            mainController.setContent(view);
            Platform.runLater(() -> {
                try { init.accept(controller);
                } catch (Exception e) { e.printStackTrace(); }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadView(String fxml) {
        loadView(fxml, Object.class, c -> {});
    }

    private static void resizeStage(double w, double h, String title) {
        Stage s = Main.getPrimaryStage();
        s.setMaximized(false);
        boolean max = (w==0 && h==0);
        double tw = max ? Screen.getPrimary().getVisualBounds().getWidth()  : w;
        double th = max ? Screen.getPrimary().getVisualBounds().getHeight() : h;

        Timeline out = new Timeline(new KeyFrame(Duration.millis(200),
                new KeyValue(s.opacityProperty(), 0.0)));
        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(e -> {
            s.setWidth(tw); s.setHeight(th);
            if (max) s.setMaximized(true);
            else {
                Rectangle2D b = Screen.getPrimary().getVisualBounds();
                s.setX((b.getWidth()-tw)/2);
                s.setY((b.getHeight()-th)/2);
            }
            s.setTitle(title);
        });
        Timeline in = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(s.opacityProperty(), 1.0)));

        new SequentialTransition(out, pause, in).play();
    }
}