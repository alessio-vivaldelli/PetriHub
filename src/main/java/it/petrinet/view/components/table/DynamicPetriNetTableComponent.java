package it.petrinet.view.components.table;

import it.petrinet.model.TableRow.PetriNetRow;
import javafx.beans.binding.Bindings;

import java.util.List;
import java.util.stream.Collectors;

public class DynamicPetriNetTableComponent extends PetriNetTableComponent {

    private static final int    MAX_ROWS      = 4;
    private static final double ROW_HEIGHT    = 60;  // altezza fissa per riga
    private static final double HEADER_HEIGHT = 30;  // altezza stimata dell’header
    private static final double MAX_HEIGHT    = HEADER_HEIGHT + ROW_HEIGHT * MAX_ROWS;

    public DynamicPetriNetTableComponent() {
        super();

        // imposta riga fissa così size*fixedCellSize funziona
        tableView.setFixedCellSize(ROW_HEIGHT);

        // override del prefHeight: HEADER + nRighe*ROW_HEIGHT, ma non più di MAX_HEIGHT
        tableView.prefHeightProperty().bind(
                Bindings.min(
                        Bindings.size(tableView.getItems())
                                .multiply(tableView.getFixedCellSize())
                                .add(HEADER_HEIGHT),
                        MAX_HEIGHT
                )
        );

        // impedisco lo scroll forzando altezza fissa
        tableView.setMaxHeight(MAX_HEIGHT);
        tableView.setMinHeight(0);
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
