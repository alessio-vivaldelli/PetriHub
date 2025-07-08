package it.petrinet;

import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.model.Arc;
import it.petrinet.petrinet.model.PLACE_TYPE;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.Transition;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PetriNetModelTest {

  @BeforeAll
  public static void initPetriNetModelTest() {
    System.out.println("Running test for PetriNetModelTest.java class...");
  }

  @AfterAll
  public static void endedPetriNetModelTest() {
    System.out.println("Ended test for PetriNetModelTest.java class!");
  }

  @Test
  void testValidNetCreation() {
    // 1. Setup: Creiamo una rete valida
    Place p1 = new Place("p1");
    p1.setType(PLACE_TYPE.START);

    Transition t1 = new Transition("t1");

    Place p2 = new Place("p2");
    p2.setType(PLACE_TYPE.END);

    Arc arc1 = new Arc("p1", "t1");
    Arc arc2 = new Arc("t1", "p2");

    // 2. Act & Assert: Il costruttore non dovrebbe lanciare eccezioni
    assertDoesNotThrow(() -> {
      new PetriNetModel("ValidNet", List.of(p1, p2), List.of(t1), List.of(arc1, arc2), p1, p2);
    });
  }

  @Test
  void testConnectionFromPlaceToPlaceThrowsException() {
    // 1. Setup: Creiamo una rete con una connessione illegale (piazza -> piazza)
    Place p1 = new Place("p1");
    p1.setType(PLACE_TYPE.START);

    Place p2 = new Place("p2");
    p2.setType(PLACE_TYPE.END);

    // 2. Act & Assert: Verifichiamo che venga lanciata l'eccezione corretta
    assertThrows(IllegalConnectionException.class, () -> {
      PetriNetModel model = new PetriNetModel();
      model.addNode(p1);
      model.addNode(p2);
      model.addArc(p1, p2); // Questa chiamata dovrebbe fallire
    }, "Una connessione da Place a Place dovrebbe lanciare IllegalConnectionException.");
  }

  @Test
  void testIllegalConnectionFromPlaceToPlaceNotInModelThrowsException() {
    // 1. Setup: Creiamo una rete con una connessione illegale (piazza -> piazza)
    Place p1 = new Place("p1");
    p1.setType(PLACE_TYPE.START);

    Transition t1 = new Transition("t1");

    Place p2 = new Place("p2");
    p2.setType(PLACE_TYPE.END);

    // 2. Act & Assert: Verifichiamo che venga lanciata l'eccezione corretta
    assertThrows(IllegalConnectionException.class, () -> {
      PetriNetModel model = new PetriNetModel();
      model.addNode(p1);
      model.addNode(t1);
      model.addArc(p1, t1);
      model.addArc(t1, p2); // il nodo p2 non è presente nel modello
    }, "Se viene fatta una connessione con un nodo non presente nel modello dovrebbe lanciarr IllegalConnectionException");
  }

  @Test
  void testNetWithoutStartNodeThrowsException() {
    Place p1 = new Place("p1");
    Transition t1 = new Transition("t1");
    Place p2 = new Place("p2"); // Non è né START né END
    p2.setType(PLACE_TYPE.END);

    Arc arc1 = new Arc("p1", "t1");
    Arc arc2 = new Arc("t1", "p2");

    // Il costruttore dovrebbe lanciare un'eccezione perché lo startNode passato è
    // null
    assertThrows(IllegalConnectionException.class, () -> {
      new PetriNetModel("NoStartNet", List.of(p1, p2), List.of(t1), List.of(arc1, arc2), null, p2);
    });
  }

  @Test
  void testNetWithNullStartNodeThrowsException() {
    Place p1 = new Place("p1");
    Transition t1 = new Transition("t1");
    Place p2 = new Place("p2");
    p2.setType(PLACE_TYPE.END);

    Place p3 = new Place("p3"); // nodo non presente nella rete

    Arc arc1 = new Arc("p1", "t1");
    Arc arc2 = new Arc("t1", "p2");

    // Il costruttore dovrebbe lanciare un'eccezione perché lo start node non è tra
    // i nodi della rete
    assertThrows(IllegalConnectionException.class, () -> {
      new PetriNetModel("NoStartNet", List.of(p1, p2), List.of(t1), List.of(arc1, arc2), p3, p2);
    });
  }

  @Test
  void testValidGetNodeByName() {

    Place p1 = new Place("p1");
    p1.setType(PLACE_TYPE.START);

    Transition t1 = new Transition("t1");
    Place p2 = new Place("p2");
    p2.setType(PLACE_TYPE.END);

    Arc arc1 = new Arc("p1", "t1");
    Arc arc2 = new Arc("t1", "p2");

    assertDoesNotThrow(() -> {
      PetriNetModel pt = new PetriNetModel("ValidNet", List.of(p1, p2), List.of(t1), List.of(arc1, arc2), p1, p2);
      pt.getNodeByName("p1");
    });
  }

  @Test
  void testGetNodeByNameException() {

    Place p1 = new Place("p1");
    p1.setType(PLACE_TYPE.START);

    Transition t1 = new Transition("t1");
    Place p2 = new Place("p2");
    p2.setType(PLACE_TYPE.END);

    Arc arc1 = new Arc("p1", "t1");
    Arc arc2 = new Arc("t1", "p2");

    assertThrows(IllegalArgumentException.class, () -> {
      PetriNetModel pt = new PetriNetModel("ValidNet", List.of(p1, p2), List.of(t1), List.of(arc1, arc2), p1, p2);
      pt.getNodeByName("p");
    }, "Cercare un nodo con un nome non presente dovrebbe lanciare IllegalArgumentException");
  }

  @Test
  void testEmptyNodeNameExcteption() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Place("");
    }, "Cerare un nodo con il nome vuoti dovrebbe lanciare IllegalArgumentException");
  }
}
