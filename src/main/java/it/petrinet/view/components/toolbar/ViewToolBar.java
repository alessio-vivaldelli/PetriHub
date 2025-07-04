package it.petrinet.view.components.toolbar;

import it.petrinet.petrinet.view.PetriNetViewerPane;
import javafx.scene.control.Button;

public class ViewToolBar extends ToolBar{

    private PetriNetViewerPane board;

    public ViewToolBar(PetriNetViewerPane board) {
        super();
        this.board = board;
        setupManageButtons();
    }

    private void setupManageButtons() {
        this.getChildren().clear();

        Button unSubScribeButton = createUnSubButton();
        Button restartButton = createRestartButton();

        this.getChildren().addAll(
                unSubScribeButton,
                restartButton,
                createGap(3)
        );


        addStaticButtons(board);
    }

    private Button createRestartButton() {
        Button restartButton = new Button();
        restartButton.setOnAction(e -> board.restartAction());
        configureButton(restartButton, "restart.png", "Restart Petri Net");
        return restartButton;
    }

    private Button createUnSubButton() {
        Button unSubScribeButton = new Button();
        unSubScribeButton.setOnAction(e -> board.unsubscribeAction());
        configureButton(unSubScribeButton, "unsubscribe.png", "Unsubscribe from Petri Net");
        return unSubScribeButton;
    }

}
