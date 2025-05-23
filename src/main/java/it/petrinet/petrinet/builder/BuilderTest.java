package it.petrinet.petrinet.builder;

import java.io.IOException;

import it.petrinet.petrinet.model.*;
import it.petrinet.petrinet.persistance.NetSerializer;
import it.petrinet.petrinet.persistance.pnml.PNMLSerializer;

public class BuilderTest {
  public static void main(String[] args) {

    PetriNetModel petriNetModel = null;
    try {
      PetriNetBuilder builder = new PetriNetBuilder("TestNet");
      petriNetModel = builder.newPlace("p1")
          .initialMarking(10)
          .donePlace()
          .newTransition("t1")
          .withType(TRANSITION_TYPE.ADMIN)
          .doneTransition()
          .addArc("p1", "t1")
          .setFinishNode("p1")
          .newPlace("p2")
          .withType(PLACE_TYPE.START)
          .donePlace()
          .build();
      System.out.println("PETRI NET CONSTRUCTED");

    } catch (Exception e) {
      System.out.println(e);
    }

    NetSerializer netSerializer = new PNMLSerializer();
    try {
      netSerializer.serialize(petriNetModel);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
