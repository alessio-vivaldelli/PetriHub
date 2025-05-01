module it.petrinet {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens it.petrinet to javafx.fxml;
    exports it.petrinet;
}