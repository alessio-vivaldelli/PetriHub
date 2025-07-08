## ViewNavigator
### Coordinator Pattern: 
Questo è forse il termine più vicino a quello che hai descritto.
Un Coordinator (o Flow Coordinator, o Application Coordinator) è un oggetto responsabile
di orchestrare il flusso di navigazione tra le diverse schermate.
Il suo scopo principale è quello di rimuovere la logica di navigazione dai View Controller (o Presenter/ViewModel),
rendendoli più semplici, riusabili e testabili. I Controller comunicano con il Coordinator (il tuo "View Navigator")
quando hanno bisogno di navigare.

### inserite altri patter usati ...