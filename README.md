# Spellbindr

Spellbindr is a modern Android application for Dungeons & Dragons 5th Edition, designed to assist both players and Dungeon Masters. It provides a clean, intuitive interface to browse D&D 5e SRD content, create and manage characters, and quickly look up spell information.

## ✨ Features

-   **📖 Spellbook:** A complete and searchable list of all spells from the D&D 5e System Reference Document (SRD). You can search, filter, and view detailed information for each spell.
-   **🧙 Character Creator:** A step-by-step wizard that guides you through the process of creating a D&D 5e character. This includes:
    -   Race and Subrace selection
    -   Class and Subclass selection
    -   Setting ability scores
    -   Choosing skills and proficiencies
    -   Selecting a background
    -   Managing equipment
    -   Adding spells
-   **👥 Character Management:** Keep track of all your characters in one place. (Functionality for viewing and managing created characters).
-   **📱 Modern UI:** Built entirely with Jetpack Compose for a modern, responsive, and slick user experience.

## 🛠️ Tech Stack

This project is built with a modern Android tech stack:

-   **Language:** 100% [Kotlin](https://kotlinlang.org/)
-   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for declarative UI development.
-   **Architecture:** Follows modern Android architecture guidelines, utilizing ViewModels and a Repository pattern.
-   **Asynchronicity:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for managing background threads.
-   **Dependency Injection:** [Hilt](https://dagger.dev/hilt/) for managing dependencies.
-   **Navigation:** [Jetpack Navigation](https://developer.android.com/guide/navigation) for handling in-app navigation.
-   **Data Persistence:** [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for storing simple key-value data.
-   **Serialization:** [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) for parsing JSON data from the SRD.

## 📦 Building from Source

To build and run the app from the source code, follow these steps:

1.  Clone the repository:
    ```bash
    git clone https://github.com/arhor/spellbindr.git
    ```
2.  Open the project in the latest stable version of [Android Studio](https://developer.android.com/studio).
3.  Let Android Studio sync the project and download the required Gradle dependencies.
4.  Build and run the app on an emulator or a physical device.

## 📜 Data Source

Spellbindr uses game content from the Dungeons & Dragons 5th Edition **System Reference Document 5.1** (SRD) provided by Wizards of the Coast. This content is available under the terms of the **Open Gaming License v1.0a**.

For more information, please see the [Open Gaming License](https://media.wizards.com/2016/downloads/DND/SRD-OGL_V5.1.pdf) from Wizards of the Coast.

## 🤝 Contributing

Contributions are welcome! If you have ideas for new features, bug fixes, or improvements, feel free to open an issue or submit a pull request.

## 📄 License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.