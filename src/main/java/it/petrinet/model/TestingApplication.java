package it.petrinet.model;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;

public class TestingApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    Graph<CustomVertex, String> g = new GraphEdgeList<>();
    CustomVertex vElement = new CustomVertex("A", "transition");
    g.insertVertex(vElement);
    CustomVertex vElement_2 = new CustomVertex("B", "transition");
    g.insertVertex(vElement_2);
    CustomVertex vElement_3 = new CustomVertex("C", "circle");
    g.insertVertex(vElement_3);
    CustomVertex vElement_4 = new CustomVertex("D", "circle");
    g.insertVertex(vElement_4);

    g.insertEdge(vElement, vElement_2, "1");
    g.insertEdge(vElement_2, vElement_3, "2");
    g.insertEdge(vElement_3, vElement_4, "3");

    SmartPlacementStrategy initialPlacement = new SmartRandomPlacementStrategy();
    SmartGraphPanel<CustomVertex, String> graphView = new SmartGraphPanel<>(g, initialPlacement);
    // SmartGraphDemoContainer container = new SmartGraphDemoContainer(graphView);

    VBox vBox = new VBox();
    Button s = new Button("Randomize shapes");
    s.setOnMouseClicked(_ -> {
      String[] t = { "transition", "circle", "star", "svg" };
      Random r = new Random();
      for (Vertex<CustomVertex> d : graphView.getModel().vertices()) {
        String shape = t[r.nextInt(0, t.length)];
        d.element().setShapeType(shape);
      }
      graphView.update();
    });

    graphView.setPrefHeight(700);
    vBox.getChildren().addAll(graphView, s);
    Scene scene = new Scene(vBox, 1024, 768);

    stage = new Stage(StageStyle.DECORATED);
    stage.setTitle("JavaFXGraph Visualization");
    stage.setScene(scene);
    stage.show();

    // graphView.setVertexSingleClickAction(vertex -> {
    // System.out.println("Click: " + vertex.getStylableLabel());
    // System.out.println("------" +
    // graphView.getStylableVertex("A"));
    // });

    // graphView.getStylableVertex("A")
    graphView.init();
    graphView.getStylableVertex(vElement_3).setStyleClass("svg_elem");

    ////    graphView.getStylableVertex("A").setStyleClass("myVertex");
    // graphView.getNodeVertex("A").setShapeType("star");
    //
    // SmartGraphVertexNode<String> s = graphView.getNodeVertex("A");

    graphView.setAutomaticLayout(true);
    graphView.update();
  }

  /**
   * A custom vertex class to represent a state.
   * <p>
   * The class is used to demonstrate the use of custom vertex types in the
   * SmartGraphPanel.
   */
  private class CustomVertex {

    private final String label;
    private String shapeType;

    public CustomVertex(String label, String type) {
      this.label = label;
      this.shapeType = type;
    }

    /**
     * Returns the name of the city.
     * 
     * @return the name of the city
     */
    @SmartLabelSource
    public String getName() {
      return label;
    }

    /*
     * Establishes the shape of the vertex to use when representing this city.
     * 
     * @return the name of the shape, see {@link
     * com.brunomnsilva.smartgraph.graphview.ShapeFactory}
     */
    @SmartShapeTypeSource
    public String modelShape() {
      return this.shapeType;
    }

    public void setShapeType(String shapeType) {
      this.shapeType = shapeType;
    }

    // @SmartRadiusSource
    // public Double modelRadius() {
    // if(this.type.equals("transition")) {
    // return 20.0;
    // }
    //
    // return 10.0;
    // }
  }

  public static void main(String[] args) {
    launch();
  }
}
