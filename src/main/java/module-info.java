module it.petrinet {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
//    requires smartgraph;
//    requires javafx.fxml;
    requires jdk.xml.dom;
    requires java.sql;
    requires java.desktop;
    requires org.slf4j;

    opens it.petrinet to javafx.fxml;
    exports it.petrinet.model;
    opens it.petrinet.model to javafx.fxml;
    exports it.petrinet.controller;
    opens it.petrinet.controller to javafx.fxml;
    exports it.petrinet.view;
    opens it.petrinet.view to javafx.fxml;
    exports it.petrinet;
    exports it.petrinet.model.Database;
    opens it.petrinet.model.Database to javafx.fxml;
    exports it.petrinet.exceptions;
    opens it.petrinet.exceptions to javafx.fxml;
    requires atlantafx.base;
    requires java.desktop;
    exports it.petrinet.view;
    exports it.petrinet.controller;
    exports it.petrinet.model;
    exports it.petrinet.view.components;
    exports it.petrinet.utils;
    opens it.petrinet.view;
    opens it.petrinet.controller;
    opens it.petrinet.model;
    opens it.petrinet.view.components;
    opens it.petrinet.utils;

}