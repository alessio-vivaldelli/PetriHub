package it.petrinet.petrinet.model;

public class Arc {

  private String from;
  private String to;

  public Arc(String from, String to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Arc) {
      Arc other = (Arc) obj;
      return this.from.equals(other.from) && this.to.equals(other.to);
    }
    return super.equals(obj);
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public void setTo(String to) {
    this.to = to;
  }

}
