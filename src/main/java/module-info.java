module it.petrinet {
    requires javafx.fxml;

    requires org.controlsfx.controls;
//    requires smartgraph;
//    requires javafx.fxml;
    requires jdk.xml.dom;
    requires atlantafx.base;
    requires java.sql;
    opens it.petrinet to javafx.fxml;
    exports it.petrinet;
}