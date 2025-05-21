module it.petrinet {
  requires javafx.fxml;

  requires org.controlsfx.controls;
  // requires smartgraph;
  // requires javafx.fxml;
  requires java.logging;
  requires jdk.xml.dom;
  requires atlantafx.base;

  opens it.petrinet to javafx.fxml;
  opens it.petrinet.controller to javafx.fxml;

  exports it.petrinet.model;
  exports it.petrinet.controller;
  exports it.petrinet;
}
