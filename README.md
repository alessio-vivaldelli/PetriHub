# Petri-Net project

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
__________

## Project description

This project is a Petri-Net simulator and editor. It allows users to create, edit, and simulate Petri-Nets using a graphical user interface (GUI). The project is built using Python and includes features for XML export and schema validation.
