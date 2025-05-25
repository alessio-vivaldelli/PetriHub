module it.petrinet {
  requires javafx.fxml;

  requires org.controlsfx.controls;
  // requires smartgraph;
  // requires javafx.fxml;
  requires jdk.xml.dom;
  requires atlantafx.base;
  requires java.sql;
  requires javafx.graphics;
  requires java.xml;

  opens it.petrinet to javafx.fxml;

  exports it.petrinet;
  exports it.petrinet.view;
  exports it.petrinet.controller;
  exports it.petrinet.model;

  opens it.petrinet.view;
  opens it.petrinet.controller;
  opens it.petrinet.model;
}
