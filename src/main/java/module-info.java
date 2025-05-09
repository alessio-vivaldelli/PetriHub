module it.petrinet {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
//    requires smartgraph;
//    requires javafx.fxml;
    requires java.logging;
    opens it.petrinet to javafx.fxml;
    exports it.petrinet;
}