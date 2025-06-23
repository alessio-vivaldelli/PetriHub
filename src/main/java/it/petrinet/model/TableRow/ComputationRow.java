package it.petrinet.model.TableRow;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class ComputationRow {
    private final StringProperty ID                        = new SimpleStringProperty();
    private final StringProperty username                  = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> initTime   = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> endTime    = new SimpleObjectProperty<>();
    private final ObjectProperty<Status> status            = new SimpleObjectProperty<>();

    public ComputationRow( String ID,
                        String username,
                        LocalDateTime initTime,
                        LocalDateTime endTime,
                        Status status)
    {
        this.ID.set(ID);
        this.username.set(username);
        this.initTime.set(initTime);
        this.endTime.set(endTime);
        this.status.set(status);
    }

    // factory method
    public static ComputationRow of(String ID,
                                    String username,
                                    LocalDateTime initTime,
                                    LocalDateTime endTime,
                                    Status status) {
        return new ComputationRow(ID, username, initTime, endTime, status);
    }

    // Property getters
    public StringProperty IDProperty()                       { return ID; }
    public StringProperty usernameProperty()                 { return username; }
    public ObjectProperty<LocalDateTime> initTimeProperty()  { return initTime; }
    public ObjectProperty<LocalDateTime> endTimeProperty()   { return endTime; }
    public ObjectProperty<Status> statusProperty()           { return status; }
}
