module it.petrinet {
  requires javafx.fxml;
  requires javafx.controls;

  requires org.controlsfx.controls;
  // requires smartgraph;
  // requires javafx.fxml;
  requires jdk.xml.dom;
  requires atlantafx.base;
  requires java.sql;
  requires javafx.graphics;
  requires java.xml;
  requires javafx.base;
  requires java.desktop;
  requires org.slf4j;
    requires jdk.jshell;
    requires jdk.compiler;

    opens it.petrinet to javafx.fxml;

  exports it.petrinet;
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

  exports it.petrinet.model.TableRow;

  opens it.petrinet.model.TableRow;

  exports it.petrinet.view.components.table;

  opens it.petrinet.view.components.table;

  exports it.petrinet.view.components.toolbar;

  opens it.petrinet.view.components.toolbar;

}
