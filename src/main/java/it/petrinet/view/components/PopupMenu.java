package it.petrinet.view.components;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Optional;

/**
 * Minimal PopupMenu with smooth animations and subtle shadow effect.
 * Features a clean, modern design with smooth entry/exit animations.
 */
public class PopupMenu {

    private final Stage ownerStage;
    private final StackPane root;

    private Stage popupStage;
    private StackPane popupRoot;
    private StackPane contentContainer;

    private String result;

    private static final Duration ANIMATION_DURATION = Duration.millis(250);
    private static final Duration FAST_ANIMATION = Duration.millis(150);

    private ChangeListener<Number> widthListener;
    private ChangeListener<Number> heightListener;
    private ChangeListener<Number> xListener;
    private ChangeListener<Number> yListener;

    public PopupMenu(Stage ownerStage) {
        this.ownerStage = ownerStage;

        Node sceneRoot = ownerStage.getScene().getRoot();
        if (sceneRoot instanceof StackPane) {
            root = (StackPane) sceneRoot;
        } else {
            root = new StackPane(sceneRoot);
            ownerStage.getScene().setRoot(root);
        }

        initializePopupStage();
        setupEventHandlers();
    }

    private void initializePopupStage() {
        popupStage = new Stage();
        popupStage.initOwner(ownerStage);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        popupRoot = new StackPane();
        popupRoot.setStyle("-fx-background-color: rgba(0 ,0,0,0);");

        contentContainer = new StackPane();
        // MODIFICA: Ombra migliorata per un effetto più "fluttuante"
        contentContainer.setStyle(
                "-fx-background-color: #1e1e2e; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 10);"
        );

        contentContainer.setMaxWidth(400);
        contentContainer.setMaxHeight(250);
        contentContainer.setMinWidth(200);
        contentContainer.setMinHeight(100);

        contentContainer.setOnMouseClicked(Event::consume);

        resetAnimationState();

        popupRoot.getChildren().add(contentContainer);
        StackPane.setAlignment(contentContainer, Pos.CENTER);

        Scene popupScene = new Scene(popupRoot, ownerStage.getWidth(), ownerStage.getHeight());
        // La scena parte completamente trasparente
        popupScene.setFill(Color.TRANSPARENT);
        popupStage.setScene(popupScene);

        setupPositionListeners();
    }

    private void setupPositionListeners() {
        widthListener = (obs, oldVal, newVal) -> popupStage.setWidth(newVal.doubleValue());
        heightListener = (obs, oldVal, newVal) -> popupStage.setHeight(newVal.doubleValue());
        xListener = (obs, oldVal, newVal) -> popupStage.setX(newVal.doubleValue());
        yListener = (obs, oldVal, newVal) -> popupStage.setY(newVal.doubleValue());
    }

    private void setupEventHandlers() {
        popupStage.setOnShown(event -> {
            popupStage.getScene().setOnKeyPressed(evt -> {
                if (evt.getCode() == KeyCode.ESCAPE) {
                    result = null;
                    closeWithAnimation();
                }
            });
        });

        popupRoot.setOnMouseClicked(evt -> {
            if (evt.getTarget() == popupRoot) {
                result = null;
                closeWithAnimation();
            }
        });

        popupStage.setOnHidden(event -> removeListeners());
    }

    private void resetAnimationState() {
        contentContainer.setOpacity(0);
        contentContainer.setScaleX(0.85);
        contentContainer.setScaleY(0.85);
        contentContainer.setTranslateY(15);
    }

    private void addListeners() {
        ownerStage.widthProperty().addListener(widthListener);
        ownerStage.heightProperty().addListener(heightListener);
        ownerStage.xProperty().addListener(xListener);
        ownerStage.yProperty().addListener(yListener);
    }

    private void removeListeners() {
        ownerStage.widthProperty().removeListener(widthListener);
        ownerStage.heightProperty().removeListener(heightListener);
        ownerStage.xProperty().removeListener(xListener);
        ownerStage.yProperty().removeListener(yListener);
    }

    public void setContent(Node content) {
        contentContainer.getChildren().setAll(content);
    }

    public Optional<String> showAndWait() {
        result = null;

        syncStagePosition();
        addListeners();
        resetAnimationState();

        // L'animazione di entrata ora include il fade-in dello sfondo
        Timeline entryAnimation = createEntryAnimation();

        // La chiamata a showAndWait() è stata spostata per evitare problemi di blocco
        entryAnimation.play();
        popupStage.showAndWait();

        // L'animazione di uscita non è più necessaria qui perché gestita da closeWithAnimation()
        return Optional.ofNullable(result);
    }

    private void syncStagePosition() {
        popupStage.setWidth(ownerStage.getWidth());
        popupStage.setHeight(ownerStage.getHeight());
        popupStage.setX(ownerStage.getX());
        popupStage.setY(ownerStage.getY());
    }

    private Timeline createEntryAnimation() {
        Timeline timeline = new Timeline();

        // MODIFICA: Aggiunta animazione per lo sfondo (overlay)
        KeyFrame overlayFadeIn = new KeyFrame(ANIMATION_DURATION,
                new KeyValue(popupStage.getScene().fillProperty(), new Color(0, 0, 0, 0), Interpolator.EASE_OUT));

        // Animazione del contenuto (invariata)
        KeyFrame contentStart = new KeyFrame(Duration.ZERO,
                new KeyValue(contentContainer.opacityProperty(), 0, Interpolator.EASE_OUT),
                new KeyValue(contentContainer.scaleXProperty(), 0.85, Interpolator.EASE_OUT),
                new KeyValue(contentContainer.scaleYProperty(), 0.85, Interpolator.EASE_OUT),
                new KeyValue(contentContainer.translateYProperty(), 15, Interpolator.EASE_OUT));

        KeyFrame contentEnd = new KeyFrame(ANIMATION_DURATION,
                new KeyValue(contentContainer.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(contentContainer.scaleXProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(contentContainer.scaleYProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(contentContainer.translateYProperty(), 0, Interpolator.EASE_OUT));

        timeline.getKeyFrames().addAll(overlayFadeIn, contentStart, contentEnd);

        return timeline;
    }

    private Timeline createExitAnimation() {
        Timeline timeline = new Timeline();

        // MODIFICA: Aggiunta animazione per lo sfondo (overlay)
        KeyFrame overlayFadeOut = new KeyFrame(FAST_ANIMATION,
                new KeyValue(popupStage.getScene().fillProperty(), Color.TRANSPARENT, Interpolator.EASE_IN));

        // Animazione del contenuto (invariata)
        KeyFrame contentStart = new KeyFrame(Duration.ZERO,
                new KeyValue(contentContainer.opacityProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(contentContainer.scaleXProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(contentContainer.scaleYProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(contentContainer.translateYProperty(), 0, Interpolator.EASE_IN));

        KeyFrame contentEnd = new KeyFrame(FAST_ANIMATION,
                new KeyValue(contentContainer.opacityProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(contentContainer.scaleXProperty(), 0.9, Interpolator.EASE_IN),
                new KeyValue(contentContainer.scaleYProperty(), 0.9, Interpolator.EASE_IN),
                new KeyValue(contentContainer.translateYProperty(), -10, Interpolator.EASE_IN));

        timeline.getKeyFrames().addAll(overlayFadeOut, contentStart, contentEnd);

        timeline.setOnFinished(e -> resetAnimationState());

        return timeline;
    }

    private void closeWithAnimation() {
        Timeline exitAnimation = createExitAnimation();
        exitAnimation.setOnFinished(e -> {
            resetAnimationState();
            // Assicura che lo sfondo torni trasparente prima della chiusura
            popupStage.getScene().setFill(Color.TRANSPARENT);
            popupStage.close();
        });
        exitAnimation.play();
    }

    public void setResult(String res) {
        this.result = res;
        closeWithAnimation();
    }

    public boolean isShowing() {
        return popupStage.isShowing();
    }

    public void setContentStyle(String style) {
        contentContainer.setStyle(style);
    }

    public void setDarkTheme(boolean dark) {
        // Mantenuto per compatibilità, ma l'aspetto è ora fisso come da richiesta
        contentContainer.setStyle(
                "-fx-background-color: #1e1e2e; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 10);"
        );
    }

    public void setOverlayStyle(String style) {
        // Mantenuto per compatibilità, ma non ha più effetto
    }
}