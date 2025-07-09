## ViewNavigator
### Coordinator Pattern: 
Questo è forse il termine più vicino a quello che hai descritto.
Un Coordinator (o Flow Coordinator, o Application Coordinator) è un oggetto responsabile
di orchestrare il flusso di navigazione tra le diverse schermate.
Il suo scopo principale è quello di rimuovere la logica di navigazione dai View Controller (o Presenter/ViewModel),
rendendoli più semplici, riusabili e testabili. I Controller comunicano con il Coordinator (il tuo "View Navigator")
quando hanno bisogno di navigare.
Design Patterns Used

    Singleton / Static Utility: The class has a private constructor and exposes all its functionality through static methods. This ensures there's only one central point of control for navigation throughout the application. It acts as a single, globally accessible service.

    Facade: The ViewNavigator provides a simple, clean API (e.g., MapsToMyNets(), LoginScene()) that hides the complex underlying operations. A single call might involve loading an FXML file, getting its controller, initializing the controller with data, managing the application window size, and setting the view in the main window. This simplifies the logic for the rest of the application.

    Mediator: The class acts as a central hub or "mediator" that allows different components to trigger navigation without needing to know about each other. For example, a button in a NavBar can communicate with ViewNavigator, which then coordinates actions between the MainController and the target controller (like ShowAllController), decoupling the components from each other.

## NetVisualController 
### State Pattern:
La classe gestisce la sua interfaccia in base a un stato interno (VisualState). Invece di un semplice switch alla creazione, ora c'è un metodo updateUiForState(VisualState newState) che centralizza tutte le modifiche all'interfaccia (toolbar, pulsante di iscrizione), rendendo le transizioni di stato più chiare e gestibili.
### Observer Pattern:
Questo pattern era già presente. Il NetVisualController agisce come "Observer" (osservatore) del PetriNetViewerPane (il "Subject" o "Observable"). Si registra per ricevere notifiche tramite setOnTransitionFired e setOnPetriNetFinished e reagisce a questi eventi.
### Factory Method (semplificato):
La logica per creare oggetti Notification era duplicata. È stata estratta in un metodo privato createAndSendNotification(...), che agisce come una factory per centralizzare e semplificare la creazione e l'invio delle notifiche.
### Single Responsibility Principle (SRP):
I metodi più grandi sono stati scomposti in metodi privati più piccoli, ognuno con una singola responsabilità. Per esempio, initialize ora orchestra la chiamata a metodi specifici come setupBoard, setupToolbar, ecc. La gestione della cronologia è completamente incapsulata nei suoi metodi (createHistoryPane, animateHistoryPane, updateAndShowHistory).
### Dependency Injection (manuale):
La modifica più importante. Invece di usare campi e metodi static per passare i dati (netModel, computation), ora si usa un metodo initData(...). L'oggetto che crea questo controller (es. un navigatore) è responsabile di "iniettare" le dipendenze necessarie dopo l'inizializzazione, un approccio molto più robusto e testabile.


Pattern utilizzati
Nel codice attuale identifico questi pattern:

MVC (Model-View-Controller) - Separazione tra logica di controllo, vista e modello
Observer Pattern - Utilizzato per gli event handlers (setOnPetriNetSaved, setOnAction)
Template Method Pattern - Nella sequenza di inizializzazione (initData → initializeComponents → setupCanvas/Toolbar/Button)
Command Pattern - Negli event handlers che incapsulano azioni
Factory Pattern (implicito) - Nella creazione del record PetriNet
Dependency Injection (parziale) - Attraverso initData