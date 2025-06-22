package it.petrinet.view.components.table;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;


public abstract class BaseTableComponent extends VBox {

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BaseTableComponent() {
        loadFXML();
        initialize();
    }

    /**
     * Load the FXML file for this component
     */
    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getFXMLPath()));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + getFXMLPath(), e);
        }
    }

    abstract public void initialize();


    abstract public String getFXMLPath();


}
