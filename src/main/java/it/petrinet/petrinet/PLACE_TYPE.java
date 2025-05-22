package it.petrinet.petrinet;

public enum PLACE_TYPE {
  NORMAL("normal"),
  START("start"),
  END("end");

  private final String value;

  PLACE_TYPE(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
