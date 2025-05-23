package it.petrinet.petrinet.model;

import it.petrinet.petrinet.IllegalConnectionException;

public class Arc {
  private String from;
  private String to;

  public Arc(String from, String to) {
    this.from = from;
    this.to = to;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }
}
