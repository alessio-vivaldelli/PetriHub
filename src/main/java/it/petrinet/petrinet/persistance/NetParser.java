package it.petrinet.petrinet.persistance;

import it.petrinet.petrinet.model.PetriNetModel;

import java.io.IOException;

public interface NetParser {
  PetriNetModel parse(String path) throws IOException;
}
