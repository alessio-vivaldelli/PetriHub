## ViewNavigator

### 📱 Coordinator Pattern
Un **Coordinator** (o “Flow/Application Coordinator”) orchestra il flusso di navigazione tra le schermate, disaccoppiando la logica di navigazione dai controller delle view, rendendoli più semplici e testabili :contentReference[oaicite:0]{index=0}.

---

### Design Pattern utilizzati

| Pattern                        | Ruolo nel refactor |
|------------------------------|--------------------|
| **Singleton / Utility statica** | `ViewNavigator` funge da classe globale con metodi statici, garantendo un unico punto di controllo per la navigazione. |
| **Facade**                   | Fornisce un’API semplice (come `toMyNets()`, `loginScene()`) che nasconde le complessità di caricamento FXML, resizing dello stage, e setup dei controller. |
| **Mediator**                 | Agisce da mediatore tra controller e `MainController`, coordinando le view senza che i componenti conoscano tra loro. |
| **Observer** *(opzionale)*   | Se `NotificationService` implementa listener, i controller (come `HomeController`) vengono notificati e aggiornano la UI automaticamente :contentReference[oaicite:1]{index=1}. |

---

## NetVisualController

- **State Pattern**  
  Usa uno stato interno (`VisualState`) e un metodo `updateUiForState(...)` per gestire l’interfaccia in base a questo stato :contentReference[oaicite:2]{index=2}.

- **Observer**  
  Registra listener su eventi (es. `setOnTransitionFired`), reagendo dinamicamente agli eventi della UI.

- **Factory Method**  
  Utilizza un metodo `createAndSendNotification(...)` per centralizzare la creazione di oggetti `Notification`.

- **SRP (Single Responsibility Principle)**  
  Spezza metodi grandi in componenti più piccoli e tematici (`setupToolbar()`, `animateHistoryPane()`, ecc.).

- **Dependency Injection (manuale)**  
  Riceve i dati necessari tramite `initData(...)` al posto di usare campi statici, migliorando testabilità e chiarezza.

---

## Altri pattern individuati

- **MVC**: separa modelli, viste e controller.
- **Template Method**: sequenza fissa di inizializzazione in `NetVisualController`.
- **Command**: azioni incapsulate nei gestori di eventi.
- **Factory**: nella creazione centralizzata delle notifiche.
- **Dependency Injection (parziale)**: tramite `initData`.

---

## 🌟 Riepilogo generale

- **ViewNavigator** = *Coordinator + Singleton + Facade + Mediator*
- **SessionContext / NotificationService** = *Singleton* (+ *Observer* se abilitati listener)
- **NetVisualController** = *State + Observer + Factory Method + SRP + Dependency Injection*
- Altri pattern: MVC, Template Method, Command

**Vantaggi:**
- **Decoupling**: componenti indipendenti e moduli isolati
- **Testabilità**: logica ben separata e testabile
- **Manutenibilità**: centralizzazione delle responsabilità
- **Estendibilità**: facilità nell’aggiungere nuovi flussi o feature

---

Se vuoi, posso aggiungere esempi di codice per l’Observer o approfondire un pattern specifico! 🎯```
::contentReference[oaicite:3]{index=3}
