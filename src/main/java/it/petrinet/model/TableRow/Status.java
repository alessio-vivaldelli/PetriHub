package it.petrinet.model.TableRow;

public enum Status {
    STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    WAITING("Waiting");

    private final String label;
    Status(String label) { this.label = label; }
    @Override public String toString() { return label; }
}