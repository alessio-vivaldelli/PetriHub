package it.petrinet.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class PetriNetRow {
    private final StringProperty name                        = new SimpleStringProperty();
    private final StringProperty author                      = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> lastModified = new SimpleObjectProperty<>();
    private final ObjectProperty<Status>     status          = new SimpleObjectProperty<>();
    private final ObjectProperty<NetCategory> type           = new SimpleObjectProperty<>();


    public enum Status {
        STARTED("Started"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        WAITING("Waiting");

        private final String label;
        Status(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public PetriNetRow( String name,
                        String author,
                        LocalDateTime lastModified,
                        Status status,
                        NetCategory type)
    {
        this.name.set(name);
        this.author.set(author);
        this.lastModified.set(lastModified);
        this.status.set(status);
        this.type.set(type);
    }

    // factory method
    public static PetriNetRow of(String name,
                              String author,
                              LocalDateTime lastModified,
                              Status status,
                              NetCategory type) {
        return new PetriNetRow(name, author, lastModified, status, type);
    }

    // Property getters
    public ReadOnlyStringProperty nameProperty()                { return name; }
    public ReadOnlyStringProperty authorProperty()              { return author; }
    public ReadOnlyObjectProperty<LocalDateTime> lastModifiedProperty() { return lastModified; }
    public ReadOnlyObjectProperty<Status> statusProperty()      { return status; }
    public ReadOnlyObjectProperty<NetCategory> typeProperty()       { return type; }
}
