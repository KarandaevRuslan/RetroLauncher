
# RetroLauncher

**A simple launcher for emulators**

## Badges

![Build](https://img.shields.io/github/actions/workflow/status/KarandaevRuslan/RetroLauncher/build.yml)
![License](https://img.shields.io/github/license/KarandaevRuslan/RetroLauncher)

## Description

RetroLauncher is a JavaFX-based application designed to simplify launching various emulators. It provides a user-friendly interface to manage and launch games via selected emulators.

## Screenshots

*Screenshots of the application interface can be added here.*

## Requirements

- **Java**: Version 8 or higher
- **Maven**: For building the project
- **Operating System**: Windows, macOS, or Linux
  
## Installation

1. Download the latest release from the [Releases](https://github.com/KarandaevRuslan/RetroLauncher/releases) page for your OS
2. Unzip the downloaded archive
3. Locate and run `RetroLauncher` script

## Building from Source

If you want to build the application from source:

1. Clone the repository:

   ```bash
   git clone https://github.com/KarandaevRuslan/RetroLauncher.git
   ```

2. Navigate to the project directory:

   ```bash
   cd RetroLauncher
   ```

3. Build the project to jlink executable using Maven:

   ```bash
   mvn clean javafx:jlink
   ```

4. Copy the script for your OS from `Scripts` directory to `target\RetroLauncher` directory
5. (Optional) Copy `RetroLauncherUpdater.jar` from `additional tools\RetroLauncherUpdater\out\artifacts` directory to `target\RetroLauncher` directory and rename it to `Updater.jar`

## Usage

After launching the application, you can:

- Add emulators to the supported list
- Add games to the corresponding list
- Configure launch parameters for each emulator and game. `{emu}` variable stores launch parameters common for entire emulator, can be added to game launch parameters string and override position of emulator launch parameters (or else if `{emu}` variable is not in use adds emulator launch parameters in the beginning of the final launch parameters string). `{rom}` variable overrides position of the game path (in the end by default)
- Change user profiles
- Change themes (light, dark) and languages (english, russian)
- Launch games through the appropriate emulators

## Testing

To run tests, execute:

```bash
mvn test
```

## Architecture

*Description of the application architecture will be added later.*

## Contributing

We welcome contributions to RetroLauncher. If you want to contribute:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/YourFeature`
3. Make your changes and commit them: `git commit -m 'Added new feature'`
4. Push your changes to the remote repository: `git push origin feature/YourFeature`
5. Create a Pull Request

Please ensure that your code adheres to the existing style and passes all tests.

## Issues and Feedback

If you find a bug or have suggestions, please create an [Issue](https://github.com/KarandaevRuslan/RetroLauncher/issues) in this repository.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](https://github.com/KarandaevRuslan/RetroLauncher/blob/master/LICENSE) file for details.

## Authors

- **Ruslan Karandaev** — *Main developer* — [KarandaevRuslan](https://github.com/KarandaevRuslan)
- **Maxim Muzalevskiy** — *Tester and Documentation writer* — [Muzalevskiy Maxim](https://github.com/rodor03)
