package it.petrinet.view.components.toolbar;

import it.petrinet.controller.NetVisualController;
import it.petrinet.exceptions.InputTypeException;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import it.petrinet.utils.IconUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class ViewToolBar extends ToolBar{

    private PetriNetViewerPane board;
    private NetVisualController controller;

    public ViewToolBar(PetriNetViewerPane board, NetVisualController controller) {
        super();
        this.board = board;
        this.controller = controller;
    }

    public void subButton() {
        Button play = createPlayButton();
        IconUtils.changeIconColor(play, Color.GRAY);
        play.setDisable(true);
        setupManageButtons(
                createGap(),
                play,
                createGap(3)
        );
    }

    public void startableButton() {
        setupManageButtons(
                createGap(),
                createPlayButton(),
                createGap(3)
        );
    }

    public void startedButton() {
        setupManageButtons(
                createUnSubButton(),
                createRestartButton(),
                createGap(3)
        );
    }

    private void setupManageButtons(Node... labels) {
        this.getChildren().clear();

        for (Node label : labels) {
            this.getChildren().add(label);
        }

        addStaticButtons(board);
    }


    private Button createRestartButton() {
        Button restartButton = new Button();
        restartButton.setOnAction(e -> controller.restartAction());
        configureButton(restartButton, "restart.png", "Restart Petri Net");
        return restartButton;
    }

    private Button createUnSubButton() {
        Button unSubScribeButton = new Button();
        unSubScribeButton.setOnAction(e -> controller.unsubscribeAction());
        configureButton(unSubScribeButton, "unsubscribe.png", "Unsubscribe from Petri Net");
        return unSubScribeButton;
    }

    private Button createPlayButton() {
        Button playButton = new Button();
        playButton.setOnAction(e -> controller.startAction());

        configureButton(playButton, "play.png", "Start Petri Net");
        return playButton;
    }

}
