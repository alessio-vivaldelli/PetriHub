package it.petrinet.utils;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for common table column setups
 */
public class TableColumnUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Setup a date column with standard formatting
     */
    public static <T> void setupDateColumn(TableColumn<T, LocalDateTime> column) {
        column.setCellFactory(col -> new TableCell<T, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                setText(empty || dateTime == null ? null : dateTime.format(DATE_FORMAT));
            }
        });
    }
}