package it.petrinet;

import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.builder.PetriNetBuilder;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.PLACE_TYPE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetriNetBuilderTest {

  private PetriNetBuilder builder;

  @BeforeEach
  void setUp() {
    // Inizializza un nuovo builder prima di ogni test
    builder = new PetriNetBuilder("TestNet");
  }

  @Test
  void testSuccessfulBuild() {
    // Test per una costruzione valida e completa
    assertDoesNotThrow(() -> {
      builder.newPlace("p1").withType(PLACE_TYPE.START).donePlace()
          .newTransition("t1").doneTransition()
          .newPlace("p2").withType(PLACE_TYPE.END).donePlace()
          .addArc("p1", "t1")
          .addArc("t1", "p2")
          .build();
    }, "La costruzione di una rete valida non dovrebbe lanciare eccezioni.");
  }

  @Test
  void testBuildReturnsCorrectModel() throws IllegalConnectionException {
    // Test per verificare che il modello costruito contenga gli elementi corretti
    PetriNetModel model = builder.newPlace("p1").withType(PLACE_TYPE.START).donePlace()
        .newTransition("t1").doneTransition()
        .newPlace("p2").withType(PLACE_TYPE.END).donePlace()
        .addArc("p1", "t1")
        .addArc("t1", "p2")
        .build();

    assertNotNull(model, "Il modello costruito non dovrebbe essere nullo.");
    assertEquals("TestNet", model.getName(), "Il nome della rete non è corretto.");
    assertEquals(3, model.getNodes().size(), "Il numero totale di nodi non è corretto.");
    assertNotNull(model.getStartNode(), "Il nodo di inizio non è stato impostato correttamente.");
    assertEquals("p1", model.getStartNode().getName());
  }

  @Test
  void testRemoveNode() throws IllegalConnectionException {
    // Test per la rimozione di un nodo
    builder.newPlace("p1").withType(PLACE_TYPE.START).donePlace()
        .newTransition("t1").doneTransition()
        .newPlace("p2").withType(PLACE_TYPE.END).donePlace()
        .addArc("p1", "t1")
        .addArc("t1", "p2")
        .newPlace("p_to_remove").donePlace();

    builder.removeNode("p_to_remove");

    assertDoesNotThrow(() -> {
      builder.build();
    });
    PetriNetModel model = builder.build();

    assertEquals(3, model.getNodes().size(), "Il nodo non è stato rimosso correttamente.");
    assertThrows(IllegalArgumentException.class, () -> {
      model.getNodeByName("p_to_remove");
    }, "Il nodo rimosso dovrebbe essere nullo nel modello finale.");
  }

  @Test
  void testBuildWithoutStartNodeThrowsException() {
    // Test per una costruzione invalida (manca il nodo di inizio)
    builder.newPlace("p1").donePlace()
        .newTransition("t1").doneTransition()
        .newPlace("p2").withType(PLACE_TYPE.END).donePlace()
        .addArc("p1", "t1")
        .addArc("t1", "p2");

    // Il metodo build() dovrebbe lanciare un'eccezione perché startNode è nullo.
    assertThrows(IllegalConnectionException.class, () -> {
      builder.build();
    }, "La costruzione di una rete senza nodo di inizio dovrebbe lanciare un'eccezione.");
  }

  @Test
  void testBuildWithDisconnectedNodeThrowsException() {
    // Test per una costruzione invalida (un nodo non è connesso)
    builder.newPlace("p1").withType(PLACE_TYPE.START).donePlace()
        .newTransition("t1").doneTransition()
        .newPlace("p2").withType(PLACE_TYPE.END).donePlace()
        .newPlace("p3_disconnected").donePlace() // Nodo non connesso
        .addArc("p1", "t1")
        .addArc("t1", "p2");

    // Il metodo build() (tramite il costruttore di PetriNetModel)
    // dovrebbe lanciare un'eccezione a causa del nodo disconnesso.
    assertThrows(IllegalConnectionException.class, () -> {
      builder.build();
    }, "La costruzione con un nodo disconnesso dovrebbe lanciare un'eccezione.");
  }
}
