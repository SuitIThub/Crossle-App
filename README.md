# Crossle-App

A mobile application that implements the Crossle puzzle game, incorporating the backend puzzle solution algorithm from [Crossle-Backend](https://github.com/SuitIThub/Crossle-Backend).

## Overview

Crossle-App is an Android application that provides an interactive interface for solving and managing Crossle puzzles. The app features both local and online database management capabilities, allowing users to work with puzzles offline and sync their progress when connected.

## Features

- Camera integration for puzzle input
- Local and online database management
- Interactive puzzle solving interface
- Progress tracking and synchronization
- Modern Material Design UI

## Technical Details

### Requirements
- Android SDK 29 (Android 10) or higher
- Java 8 compatibility
- Internet connection for online features

### Dependencies
- AndroidX libraries
- Material Design components
- Navigation components
- OkHttp for network requests
- MongoDB integration
- JSON processing libraries
- SLF4J for logging

### Project Structure
- `MainActivity.java`: Main entry point of the application
- `CamActivity.java`: Camera integration for puzzle input
- `WorkActivity.java`: Puzzle solving interface
- `LocalDBManager/`: Local database management
- `OnlineDBManager/`: Online database synchronization
- `classes/`: Core application classes

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Build and run the application

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the terms of the MIT license.
