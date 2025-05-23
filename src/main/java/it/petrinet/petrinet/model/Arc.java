package it.petrinet.petrinet.model;

import it.petrinet.petrinet.IllegalConnectionException;

public class Arc {
  private Node from;
  private Node to;

  public Arc(Node from, Node to) throws IllegalConnectionException {

    if (from.getClass() == to.getClass()) {
      throw new IllegalConnectionException();
    }
    this.from = from;
    this.to = to;
  }

  public Node getFrom() {
    return from;
  }

  public Node getTo() {
    return to;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Arc f) {
      return this.getFrom().equals(f.getFrom()) &&
          this.getTo().equals(f.getTo());
    }
    return false;
  }

}
