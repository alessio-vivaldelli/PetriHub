package it.petrinet.model.TableRow;

public enum Status {
    WAITING("Waiting"),
    IN_PROGRESS("In Progress"),
    NOT_STARTED("Not Started"),
    COMPLETED("Completed");

    private final String label;
    Status(String label) { this.label = label; }
    @Override public String toString() { return label; }
}