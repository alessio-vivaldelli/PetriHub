package it.petrinet.petrinet.builder;

import it.petrinet.petrinet.model.*;

public class BuilderTest {
  public static void main(String[] args) {

    PetriNetModel petriNetModel;
    try {
      PetriNetBuilder builder = new PetriNetBuilder("TestNet");
      petriNetModel = builder.newPlace("p1")
          .initialMarking(10)
          .withType(PLACE_TYPE.START)
          .donePlace()
          .newTransition("t1")
          .withType(TRANSITION_TYPE.ADMIN)
          .doneTransition()
          .addArc("p1", "t1")
          .build();
      System.out.println("PETRI NET CONSTRUCTED");

    } catch (Exception e) {
      System.out.println(e);
    }

  }
}
