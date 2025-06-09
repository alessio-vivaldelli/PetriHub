package it.petrinet.view.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

/**
 * TableElement rappresenta una rete con proprietà osservabili:
 * - name: nome della rete
 * - author: autore
 * - lastModified: data di ultima modifica
 * - status: stato della rete (enum Status)
 * - type: categoria della rete (enum NetCategory)
 */
public class TableElement {

    /**
     * Stati possibili di una rete
     */
    public enum Status {
        completed,
        notStarted,
        waiting,
        inProgress;
    }

    private final StringProperty name;
    private final StringProperty author;
    private final ObjectProperty<LocalDate> lastModified;
    private final ObjectProperty<Status> status;
    private final ObjectProperty<NetCategory> type;

    /**
     * Costruttore di TableElement
     * @param name nome della rete
     * @param author autore della rete
     * @param lastModified data di ultima modifica
     * @param status stato della rete
     * @param type categoria della rete
     */
    public TableElement(String name,
                        String author,
                        LocalDate lastModified,
                        Status status,
                        NetCategory type) {
        this.name = new SimpleStringProperty(name);
        this.author = new SimpleStringProperty(author);
        this.lastModified = new SimpleObjectProperty<>(lastModified);
        this.status = new SimpleObjectProperty<>(status);
        this.type = new SimpleObjectProperty<>(type);
    }

    // --- Proprietà name ---
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    // --- Proprietà author ---
    public String getAuthor() { return author.get(); }
    public StringProperty authorProperty() { return author; }
    public void setAuthor(String author) { this.author.set(author); }

    // --- Proprietà lastModified ---
    public LocalDate getLastModified() { return lastModified.get(); }
    public ObjectProperty<LocalDate> lastModifiedProperty() { return lastModified; }
    public void setLastModified(LocalDate date) { this.lastModified.set(date); }

    // --- Proprietà status ---
    public Status getStatus() { return status.get(); }
    public ObjectProperty<Status> statusProperty() { return status; }
    public void setStatus(Status status) { this.status.set(status); }

    // --- Proprietà type ---
    public NetCategory getType() { return type.get(); }
    public ObjectProperty<NetCategory> typeProperty() { return type; }
    public void setType(NetCategory type) { this.type.set(type); }

    @Override
    public String toString() {
        return String.format("TableElement{name='%s', author='%s', lastModified=%s, status=%s, type=%s}",
                getName(), getAuthor(), getLastModified(), getStatus(), getType());
    }
}
