# StormyAi

A production-ready Android application for AI image and video generation using the Novita.ai API.

## Features

- **Text-to-Image Generation**: Create images from text prompts
- **Text-to-Video Generation**: Generate videos from text descriptions
- **Model Selection**: Choose from multiple AI models
- **Advanced Settings**: Control dimensions, steps, CFG scale, and more
- **Generation History**: Browse and manage all your creations
- **Local Storage**: Save and view generated content offline
- **Modern UI**: Material 3 design with Jetpack Compose
- **Dark Mode**: Full dark theme support

## Architecture

The app follows **Clean Architecture** principles with clear separation of concerns:

```
com.stormyai.app/
├── common/              # Shared utilities and constants
├── data/
│   ├── local/           # Room database, DAOs, entities
│   ├── remote/          # Retrofit API service, DTOs, interceptors
│   ├── repository/      # Repository implementations
│   └── di/              # Dependency injection modules
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic use cases
└── presentation/
    ├── theme/           # Material 3 theming
    ├── navigation/      # Navigation graph
    ├── generate/        # Generation screen and ViewModel
    ├── history/         # History screen and ViewModel
    └── settings/        # Settings screen and ViewModel
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Async**: Coroutines + Flow
- **Image Loading**: Coil
- **Storage**: DataStore Preferences

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or newer
- Android SDK 34 (API level 34)
- Gradle 8.4

### Setup

1. **Clone the repository**:
```bash
git clone <repository-url>
cd StormyAi
```

2. **Generate the Gradle wrapper**:
```bash
gradle wrapper --gradle-version 8.4
```

3. **Open in Android Studio**:
- File → Open → Select the project directory
- Wait for Gradle sync to complete

4. **Build the project**:
```bash
./gradlew assembleDebug
```

### API Key Setup

1. Sign up at [novita.ai](https://novita.ai) to get an API key
2. Open the app and navigate to Settings
3. Enter your API key in the API Configuration section
4. Click "Validate" to verify your key
5. Click "Save Settings"

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Running Tests
```bash
./gradlew test
```

### Lint Checks
```bash
./gradlew lint
```

## Project Structure

### Key Files

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | App-level build configuration |
| `build.gradle.kts` | Project-level build configuration |
| `settings.gradle.kts` | Project settings and module declarations |
| `domain/model/Models.kt` | Domain models (GenerationTask, HistoryItem, etc.) |
| `data/remote/NovitaApiService.kt` | Retrofit API interface |
| `data/local/AppDatabase.kt` | Room database configuration |
| `presentation/navigation/NavGraph.kt` | Main navigation graph |

### API Endpoints Used

| Feature | Endpoint | Cost |
|---------|----------|------|
| Text-to-Image | `POST /v3/async/txt2img` | ~$0.002-0.005/image |
| Image-to-Image | `POST /v3/async/img2img` | ~$0.003-0.006/image |
| Text-to-Video | `POST /v3/async/txt2video` | ~$0.02-0.05/video |
| Task Status | `GET /v3/async/task/{taskId}` | Free |

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Novita.ai](https://novita.ai) for providing the AI generation API
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the modern UI toolkit
- [Hilt](https://dagger.dev/hilt/) for dependency injection
