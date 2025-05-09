module it.petrinet {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires smartgraph;

    opens it.petrinet to javafx.fxml;
    exports it.petrinet;
}