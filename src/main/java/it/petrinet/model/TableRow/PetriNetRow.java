package it.petrinet.model.TableRow;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;

public class PetriNetRow {
    private final StringProperty name                        = new SimpleStringProperty();
    private final StringProperty author                      = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> lastModified = new SimpleObjectProperty<>();
    private final ObjectProperty<Status>     status          = new SimpleObjectProperty<>();
    private final ObjectProperty<NetCategory> type           = new SimpleObjectProperty<>();

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
    public StringProperty nameProperty()                         { return name; }
    public StringProperty authorProperty()                       { return author; }
    public ObjectProperty<LocalDateTime> lastModifiedProperty()  { return lastModified; }
    public ObjectProperty<Status> statusProperty()               { return status; }
    public ObjectProperty<NetCategory> typeProperty()            { return type; }
}
