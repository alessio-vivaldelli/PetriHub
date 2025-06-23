package it.petrinet.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

public class BadgeCell<T, E extends Enum<E>> extends TableCell<T, E> {
    private final Label badge = new Label();
    private final Category category;

    public enum Category {
        STATUS,
        TYPE
    }

    public BadgeCell(Category category) {
        this.category = category;
        badge.getStyleClass().add("badge");
    }

    @Override
    protected void updateItem(E item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            String text = item.toString();
            badge.setText(text);
            // rimuovi vecchie classi di questa categoria
            String prefix = "badge-" + category.name().toLowerCase() + "-";
            badge.getStyleClass().removeIf(cls -> cls.startsWith(prefix));
            // genera suffisso: item.name().toLowerCase()
            String key = item.name().toLowerCase();
            badge.getStyleClass().add(prefix + key);
            setGraphic(badge);
        }
    }
}