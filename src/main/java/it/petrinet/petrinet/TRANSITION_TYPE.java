package it.petrinet.petrinet;

public enum TRANSITION_TYPE {
  USER("user"),
  ADMIN("admin");

  private final String value;

  TRANSITION_TYPE(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
