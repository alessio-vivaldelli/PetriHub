# Petri-Net project

<!--toc:start-->

- [Petri-Net project](#petri-net-project)
  - [Project structure](#project-structure)
  - [Project description](#project-description)
  - [PNML](#pnml)
  <!--toc:end-->

## Project structure

```mermaid
gitGraph
   commit id: "Initial commit"
   branch develop
   checkout develop
   commit id: "Configure CI/CD"
   branch petrinet
   checkout petrinet
   commit id: "Core PetriNet model"
   branch feature/xml
   checkout feature/xml
   commit id: "Add XML export"
   commit id: "XML schema validation"
   checkout petrinet
   branch feature/gui
   checkout feature/gui
   commit id: "Basic GUI skeleton"
   commit id: "Graphical PetriNet editor"
   checkout petrinet
   merge feature/xml id: "Merge XML feature"
   checkout develop
   merge petrinet id: "Integrate PetriNet core & features"
   checkout main
   checkout develop
   branch editor
   checkout editor
   commit id: "Login UI"
   checkout develop
   merge editor
   checkout main
   merge develop id: "Release v1.0"
```

---

## Project description

This project is a Petri-Net simulator and editor. It allows users to create, edit, and simulate Petri-Nets using a graphical user interface (GUI). The project is built using Python and includes features for XML export and schema validation.

## PNML

PNML (Petri Net Markup Language) is an XML-based format for representing Petri nets. It provides a standardized way to describe the structure and behavior of Petri nets, making it easier to share and exchange Petri net models between different tools and applications.


```mermaid
classDiagram
    %% Domain Model

    Place "1" *-- "1" ToolSpecificMetadata : metadata
    Transition "1" *-- "1" ToolSpecificMetadata : metadata

    %% Persistence Interfaces & Implementations
    class NetSerializer {
        + serialize(PetriNetModel, OutputStream)
    }
    class PNMLSerializer {
        + serialize(PetriNetModel, OutputStream)
        + writePlace(Place)
        + writeTransition(Transition)
    }

    class NetParser {
        + parse(InputStream) PetriNetModel
    }
    class PNMLParser {
        + parse(InputStream) PetriNetModel
    }

    XmlLibraryAdapter <.. PNMLSerializer : uses
    XmlLibraryAdapter <.. PNMLParser     : uses

    NetSerializer <|.. PNMLSerializer
    NetParser     <|.. PNMLParser

    %% Factory & Builder
    class NodeFactory {
        + createNode(String type, Map~String,String~ attrs) Node
    }
    class PetriNetBuilder {
        + startNet(String id)
        + withName(String)
        + addPlace(String id) PetriNetBuilder
        + addTransition(String id) PetriNetBuilder
        + markAsStartNode()
        + markAsFinishNode()
        + build() PetriNetModel
    }

    PNMLParser ..> NodeFactory         : creates
    PNMLParser ..> PetriNetBuilder     : builds
    NodeFactory --|> Place
    NodeFactory --|> Transition

    %% Metadata & Utils
    class ToolSpecificMetadata {
        - String toolName
        - String version
        - Map~String,String~ data
        + put(String key, String value)
        + get(String key) String
    }
    class XmlLibraryAdapter {
        <<adapter>>
        + parseDocument(InputStream)
        + writeDocument(Document, OutputStream)
    }
```