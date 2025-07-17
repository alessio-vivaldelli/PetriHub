# PetriNet Hub
> Final project for my Software Engineering course @ UniVR &mdash; A.Y. 2024/2025. The complete documentation, including class and sequence diagram, (written in Italian) can be found [here](https://github.com/alessio-vivaldelli/PetriHub/Relazione-PetriHub.pdf).
## Description

**PetriNet Hub** is a comprehensive desktop application developed in Java for the design, visualization, and simulation of Petri nets, following the formalisms of the **ISO/IEC 15909-2** standard to save and load them.

The application is built with a clean, modern user interface and a robust backend, allowing users to manage their own library of Petri nets, interact with nets created by others, and simulate their behavior step-by-step.

### Key Features

* **User and Session Management**: A complete login and registration system to manage user access. The application supports two distinct roles: **standard users** and **administrators**, with administrators having elevated privileges to create new nets and fire admin transition.
* **Interactive Petri Net Editor**: A powerful graphical editor that allows users to:
    * Create, position, and label **places** and **transitions**.
    * Draw **arcs** to connect nodes, with built-in validation to enforce Petri net rules (e.g., preventing place-to-place connections).
    * Designate special nodes, such as **start** and **end** places and changing transition type between **user** and **admin**.
* **Visual Simulator**: A viewer mode where users can load a Petri net and simulate its execution. The visualizer graphically indicates which transitions are "firable" and updates the token count in each place as transitions are fired.
* **PNML-Standard Persistence**: Petri nets are saved and loaded using the **PNML (Petri Net Markup Language)** format. The system also extends the standard to save custom application-specific data, such as the type of transition (e.g., `USER` or `ADMIN`).
* **Centralized Dashboard**: Upon logging in, users are greeted with a dashboard that provides a clear overview of their recently modified nets, subscribed nets, and other nets available to discover on the platform and also view notifications inbox.
* **Database Backend**: All user data, net metadata, and simulation states are persisted in a local **SQLite** database, making the application self-contained and easy to run.

---

## Dependencies

This project is managed by **Apache Maven**. All dependencies are listed in the `pom.xml` file. The primary technologies used are:

* **Java 17+**: The core programming language.
* **JavaFX**: For the entire Graphical User Interface (GUI).
* **JavaFXSmartGraph**: A third-party library used as a foundation for graph visualization. This library was integrated locally and **modified** to support custom node shapes, advanced event handling, and direct rendering of domain-specific objects (`Place`, `Transition`).
* **JUnit 5**: For the comprehensive suite of unit tests.
* **SQLite-JDBC**: The driver used for database connectivity.

---

## Usage and Installation

To get the application running on your local machine, please follow these steps.

### Prerequisites

* **JDK 17** or higher.
* **Apache Maven** installed and configured in your system's PATH.

### Running the Application

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/alessio-vivaldelli/PetriHub
    cd PetriHub
    ```

2.  **Build and Run with Maven:**
    The project uses the `javafx-maven-plugin`, so you can compile and run the application with a single command.
    ```sh
    mvn clean javafx:run
    ```
    This will launch the application, starting with the login screen.

### Running Tests

The project includes a suite of unit tests to ensure the core logic is working correctly. To run these tests, execute the following Maven command:
```sh
mvn test