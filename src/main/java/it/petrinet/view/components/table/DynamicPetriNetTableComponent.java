package it.petrinet.view.components.table;

import it.petrinet.model.TableRow.PetriNetRow;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;

import java.util.List;
import java.util.stream.Collectors;

public class DynamicPetriNetTableComponent extends PetriNetTableComponent {

    private static final int    MAX_ROWS      = 4;
    private static final double ROW_HEIGHT    = 60;  // altezza fissa per riga
    private static final double HEADER_HEIGHT = 30;  // altezza stimata dell’header
    private static final double MAX_HEIGHT    = HEADER_HEIGHT + ROW_HEIGHT * MAX_ROWS;
    private static final double MIN_TABLE_HEIGHT_NO_ROWS = 272.0; // Altezza quando non ci sono righe

    public DynamicPetriNetTableComponent() {
        super();

        // imposta riga fissa così size*fixedCellSize funziona
        tableView.setFixedCellSize(ROW_HEIGHT);

        // override del prefHeight: HEADER + nRighe*ROW_HEIGHT, ma non più di MAX_HEIGHT
        // Aggiungi una condizione per l'altezza minima quando non ci sono righe
        DoubleBinding calculatedHeight = Bindings.min(
                Bindings.size(tableView.getItems())
                        .multiply(tableView.getFixedCellSize())
                        .add(HEADER_HEIGHT),
                MAX_HEIGHT
        );

        tableView.prefHeightProperty().bind(
                Bindings.when(Bindings.size(tableView.getItems()).isEqualTo(0))
                        .then(MIN_TABLE_HEIGHT_NO_ROWS)
                        .otherwise(calculatedHeight)
        );

        // impedisco lo scroll forzando altezza fissa
        tableView.setMaxHeight(MAX_HEIGHT);
        tableView.setMinHeight(0); // Puoi anche considerare di impostare MIN_TABLE_HEIGHT_NO_ROWS qui se vuoi una vera altezza minima complessiva
    }

    @Override
    public void setData(List<PetriNetRow> rows) {
        // tronco a MAX_ROWS per non far comparire la scrollbar
        List<PetriNetRow> truncated = rows.stream()
                .limit(MAX_ROWS)
                .collect(Collectors.toList());
        super.setData(truncated);
    }
}