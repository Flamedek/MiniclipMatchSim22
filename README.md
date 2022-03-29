# About
Introduction assignment for GameBascis BV, March 2022.

# :open_file_folder: Packages
- `/data` - Data layer and models
- `/simulation` - Match engine and helpers
  - `/behaviour` - Match simulation events and behaviours
- `/ui` - Interface and ViewModels
- `/util` - static utilities

# :construction: State of the project
This is a technical demo. Development had a limited time scope, made mostly over a single weekend.  
The Match Simulation is implemented as a framework, that could be expanded to include many more factors such as tactics and detailed player statistics.  
Some of these points are marked with TODO comments. Given enough development time, simulation could improve to be more and more realistic.

# :hammer: Tools
Run `/data/model/GenerateDemoData.kt` as a standalone Kotlin program to generate demo team and player data.

# :white_check_mark: Tests
Includes some unit tests for Repositories, ViewModels and utility classes. 

# Shadow
The https://github.com/pavelsemak/alpha-movie dependencie is shadowed into `/ui/components/alphamovie`, since it was only published to the jcenter repository which is no longer available. It is used for an easter egg in the results stage :egg:. 
