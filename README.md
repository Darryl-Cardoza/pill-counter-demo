
# MobRite Pill Counting Application - Android


A sophisticated Android application designed to count and manage pharmaceutical pills using advanced ML Kit technology and barcode scanning capabilities. The app leverages TensorFlow Lite for on-device AI-powered pill detection and integrates with healthcare systems via HL7 protocol.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Setup & Installation](#setup--installation)
- [Build & Deployment](#build--deployment)
- [API Integration](#api-integration)
- [Database](#database)
- [Security](#security)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Project Overview

**Pill Counting New Models** is an Android application developed for CCRL India that combines computer vision, barcode scanning, and healthcare interoperability to streamline pharmaceutical pill counting operations. The app uses TensorFlow Lite models for on-device pill detection with GPU acceleration support and integrates with hospital systems via HL7 messaging protocol.

### Key Metrics
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Compile SDK**: Android 15 (API 36)
- **Version**: 1.0.0
- **Application ID**: com.rite.pillcounting
- **Language**: Kotlin + Jetpack Compose

---

## ✨ Key Features

### 1. **Pill Detection & Counting**
   - Real-time pill detection using TensorFlow Lite GPU-accelerated models
   - On-device AI processing (no cloud dependency)
   - Camera integration via CameraX
   - Support for multiple pill detection models

### 2. **Barcode Scanning**
   - ML Kit barcode scanning integration
   - Support for various barcode formats (Code 128, EAN-13, UPC, etc.)
   - Real-time barcode detection from camera feed

### 3. **Transaction Management**
   - Create and manage pill count transactions
   - Transaction history tracking
   - Sync unsynced transactions to backend
   - Resume interrupted transactions

### 4. **Healthcare System Integration**
   - HL7 messaging protocol support
   - Network Service Discovery (NSD) for device communication
   - MLLP (Minimal Lower Layer Protocol) implementation
   - Real-time data sync with hospital systems

### 5. **User Management**
   - Secure user authentication with PIN verification
   - Role-based access control
   - User profile management
   - Session management with token refresh

### 6. **Data Security**
   - AES encryption for sensitive data (credentials, encryption keys)
   - Secure encryption/decryption for TensorFlow Lite models
   - Network security configuration for HTTPS/TLS
   - BouncyCastle for PKI operations

### 7. **Notifications**
   - Firebase Cloud Messaging (FCM) integration
   - Push notification support
   - In-app notification handling

### 8. **Dashboard & Analytics**
   - Real-time statistics and metrics
   - Transaction summary and status overview
   - User activity tracking
   - HL7 message status monitoring

### 9. **Location Services**
   - GPS location capture
   - Location-based features using Google Play Services

### 10. **Settings & Configuration**
   - Customizable application settings
   - Server URL and API key management
   - Application preferences and user settings
   - Theme and UI customization

---

## 🛠 Tech Stack

### Core Framework
- **Android Framework**: AndroidX libraries
- **Language**: Kotlin 1.9.10
- **Build System**: Gradle 8.1.4 with Kotlin DSL
- **UI Framework**: Jetpack Compose
- **Java Target**: JVM 11

### Architecture & DI
- **Dependency Injection**: Dagger Hilt 2.51
- **Architecture Pattern**: MVVM + Clean Architecture
- **Navigation**: Jetpack Navigation

### Asynchronous Programming
- **Coroutines**: kotlinx-coroutines 1.7.3 + 1.8.0
- **Reactive**: Flow + StateFlow
- **Testing Utilities**: Turbine 1.1.0

### Networking
- **HTTP Client**: Retrofit 2.9.0 + OkHttp 4.12.0
- **JSON Serialization**: Moshi 1.15.0 + Kotlinx Serialization 1.6.3
- **Debugging**: Chucker 4.0.0
- **Server Framework**: Ktor Server 2.3.12

### Database
- **Local Storage**: Room 2.6.1 (SQLite)
- **Data Access Objects**: Room DAOs for type-safe database access

### AI/ML & Computer Vision
- **TensorFlow Lite**: 2.17.0
  - GPU Delegate for accelerated inference
  - Support for GPU API
  - Model support library
- **ML Kit**: Barcode scanning 17.2.0
- **CameraX**: 1.4.0
  - Camera core, camera2 provider, lifecycle integration, view component

### Security & Encryption
- **Encryption**: AndroidX Security-Crypto 1.1.0-alpha06
- **PKI Libraries**: BouncyCastle
  - bcprov-jdk18on 1.83 (Cryptography provider)
  - bcpkix-jdk18on 1.83 (PKI extensions)
- **Network Security**: Network security configuration files

### Backend Integration
- **Firebase**: Firebase BOM 33.2.0
  - Cloud Messaging (FCM)
  - Analytics
- **Google Play Services**: 
  - Location services 21.3.0

### UI Components
- **Material Design 3**: Material3 1.3.2
- **Material Icons**: Material-icons-extended 1.7.8
- **Image Loading**: Coil-Compose 2.7.0
- **Calendar**: Kizitonwose Calendar Compose 2.5.0
- **Permissions**: Accompanist-permissions 0.28.0

### Testing
- **Unit Testing**:
  - JUnit 4.13.2
  - Mockito 5.17.0 + mockito-kotlin 5.2.1
  - MockK 1.13.8
  - Coroutines Test 1.8.0
  - Turbine 1.1.0
  
- **Instrumentation Testing**:
  - Espresso 3.5.1
  - AndroidX Test JUnit 1.1.5
  - Compose UI Test 1.7.8
  - Navigation Testing 2.9.4
  - Mockito Android 5.4.0

### Utilities
- **JSON Processing**: GSON 2.10.1 + JSON 20230227
- **Logging**: Custom AppLogger wrapper

---

## 🏗 Architecture

The application follows **MVVM (Model-View-ViewModel)** and **Clean Architecture** principles:

```
┌─────────────────────────────────────────┐
│         UI Layer (Presentation)         │
│  ┌──────────────────────────────────┐   │
│  │   Jetpack Compose UI Components   │   │
│  │   - Screens, Composables          │   │
│  │   - Theme & Styling               │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│     ViewModel & State Management         │
│  ┌──────────────────────────────────┐   │
│  │   ViewModels (Lifecycle-aware)    │   │
│  │   State: Flow, StateFlow          │   │
│  │   Event Handling                  │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│    Domain Layer (Business Logic)        │
│  ┌──────────────────────────────────┐   │
│  │   Use Cases / Interactors        │   │
│  │   Domain Models & Entities       │   │
│  │   Repository Interfaces          │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│      Data Layer (Persistence)           │
│  ┌──────────────────────────────────┐   │
│  │   Room Database (Local)           │   │
│  │   Retrofit API Client (Remote)    │   │
│  │   Repositories (Implementation)   │   │
│  │   Data Models & DTOs              │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│     Core Infrastructure Layer            │
│  ┌──────────────────────────────────┐   │
│  │   Security & Encryption          │   │
│  │   API Configuration              │   │
│  │   Logger & Utils                 │   │
│  │   DI Container (Hilt)            │   │
│  │   HL7 & Networking               │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### Dependency Injection (Hilt)
- All major components are injected via Hilt
- Modules defined in `core/di/` for configuration
- Application-scoped and singleton instances for shared resources

---

## 📁 Project Structure

```
pill-counter-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # App permissions & configuration
│   │   │   ├── assets/                      # App resources
│   │   │   ├── java/com/rite/pillcounting/
│   │   │   │   ├── MainActivity.kt           # Main entry point
│   │   │   │   ├── PillCountingApplication.kt # App initialization
│   │   │   │   │
│   │   │   │   ├── core/                    # Core infrastructure
│   │   │   │   │   ├── api/
│   │   │   │   │   │   └── interfaceDetail/  # Retrofit API interfaces
│   │   │   │   │   ├── di/                  # Dependency injection modules
│   │   │   │   │   ├── hl7/                 # HL7 protocol implementation
│   │   │   │   │   │   ├── core/             # HL7 core utilities
│   │   │   │   │   │   ├── hl7MessageHandler/# Message processing
│   │   │   │   │   │   ├── imageWebService/  # Image transfer service
│   │   │   │   │   │   ├── mllp/             # MLLP protocol
│   │   │   │   │   │   └── service/          # HL7 service
│   │   │   │   │   ├── models/              # Shared data models
│   │   │   │   │   ├── room/                # Room database
│   │   │   │   │   │   ├── dao/             # Data Access Objects
│   │   │   │   │   │   ├── di/              # DB dependency injection
│   │   │   │   │   │   ├── models/          # Room entities
│   │   │   │   │   │   └── AppDatabase.kt   # Main DB definition
│   │   │   │   │   ├── security/            # Encryption & security
│   │   │   │   │   ├── settings/            # Preference management
│   │   │   │   │   ├── refreshToken/        # Token refresh logic
│   │   │   │   │   └── utils/               # General utilities
│   │   │   │   │       ├── common/
│   │   │   │   │       ├── compose/
│   │   │   │   │       ├── logger/
│   │   │   │   │       ├── notification/
│   │   │   │   │       ├── preference/
│   │   │   │   │       └── ...
│   │   │   │   │
│   │   │   │   ├── feature/                 # Feature modules (by domain)
│   │   │   │   │   ├── barcodeScan/         # Barcode scanning feature
│   │   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── di/
│   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   └── presentation/
│   │   │   │   │   ├── pillCountScan/       # Pill detection feature
│   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   │   ├── PillDetectionModelLoader.kt
│   │   │   │   │   │   │   ├── data/
│   │   │   │   │   │   │   └── model/
│   │   │   │   │   │   └── presentation/
│   │   │   │   │   │       └── logic/
│   │   │   │   │   ├── login/               # Authentication
│   │   │   │   │   ├── dashboard/           # Dashboard feature
│   │   │   │   │   ├── countResume/         # Resume transactions
│   │   │   │   │   ├── history/             # Transaction history
│   │   │   │   │   ├── menu/                # Main menu
│   │   │   │   │   ├── profile/             # User profile
│   │   │   │   │   ├── settings/            # App settings
│   │   │   │   │   ├── hl7/                 # HL7 features
│   │   │   │   │   ├── unsyncedTransaction/ # Unsynced data
│   │   │   │   │   └── verifyPin/           # PIN verification
│   │   │   │   │
│   │   │   │   ├── navigation/              # Navigation graphs
│   │   │   │   └── ui/
│   │   │   │       └── theme/               # Compose theme & colors
│   │   │   │
│   │   │   └── res/                         # Android resources
│   │   │       ├── drawable/
│   │   │       ├── mipmap/
│   │   │       ├── values/
│   │   │       ├── xml/
│   │   │       └── ...
│   │   │
│   │   ├── androidTest/                     # Instrumentation tests
│   │   └── test/                            # Unit tests
│   │
│   ├── build.gradle.kts                    # App-level build configuration
│   ├── google-services.json                 # Firebase configuration
│   ├── proguard-rules.pro                   # ProGuard obfuscation rules
│   └── build/                               # Build artifacts (generated)
│
├── gradle/
│   └── wrapper/                             # Gradle wrapper
├── build.gradle.kts                        # Root-level build config
├── settings.gradle.kts                     # Settings & included modules
├── gradle.properties                       # Gradle properties
├── local.properties                        # Local configuration (gitignored)
├── gradlew                                 # Gradle wrapper script
├── gradlew.bat                             # Gradle wrapper batch script
└── README.md                               # This file
```

### Module Organization

**Core Module** (`core/`): Infrastructure and cross-cutting concerns
- **api/**: Retrofit API interfaces for backend communication
- **di/**: Hilt dependency injection configuration
- **hl7/**: Healthcare interoperability layer
- **models/**: Shared data models used across features
- **room/**: Local database layer with Room DAOs and entities
- **security/**: Encryption, decryption, and security utilities
- **settings/**: Preference management and app configuration
- **refreshToken/**: Token lifecycle management
- **utils/**: Common utilities, logging, notifications, and helpers

**Feature Modules** (`feature/`): Business logic organized by feature
- Each feature follows MVVM architecture
- Clean separation between data, domain, and presentation layers
- Independent feature navigation

---

## 🚀 Setup & Installation

### Prerequisites
- Android Studio (Latest)
- Android SDK 36
- JDK 11 or higher
- Gradle 8.1.4 (included via wrapper)
- Firebase account (for FCM)

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd pill-counter-android
```

### Step 2: Configure Local Properties
Create `local.properties` in the project root:
```properties
sdk.dir=/path/to/android/sdk
```

### Step 3: Firebase Setup
1. Place `google-services.json` in the `app/` directory
2. Ensure Firebase project is configured in Firebase Console
3. Enable FCM in Firebase Console

### Step 4: Build Configuration
The app uses build fields for API configuration. These are set in `app/build.gradle.kts`:
- `SERVER_KEY`: Encryption key for API communication
- `BASE_URL`: Backend API endpoint (default: https://pill.ccrlindia.com/)

### Step 5: Build the Project
```bash
# Debug build
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease
```

### Step 6: Run on Device/Emulator
```bash
# Install and run debug APK
./gradlew :app:installDebug
adb shell am start -n com.rite.pillcounting/.MainActivity
```

---

## 🔨 Build & Deployment

### Build Types

**Debug Build**
- Unit test coverage enabled
- Debugging symbols included
- Longer build time but full debugging support

**Release Build**
- ProGuard obfuscation enabled
- Resource shrinking enabled
- Optimized for production
- Code size minimization

### Build Variants
The app supports different configurations:
- **Build Flavor**: Not explicitly defined (single flavor)
- **Compile Options**: Java 11 target with Kotlin JVM target 11

### ProGuard Configuration
Rules defined in `app/proguard-rules.pro`:
- Preserves library APIs used by reflection
- Optimizes code while maintaining functionality
- Excludes specific classes from obfuscation

### Building APK
```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK (requires signing configuration)
./gradlew :app:assembleRelease

# Build and install directly
./gradlew :app:installDebug
```

### Building AAB (Android App Bundle) for Play Store
```bash
./gradlew :app:bundleRelease
```

---

## 🌐 API Integration

### Base Configuration
- **Base URL**: `https://pill.ccrlindia.com/`
- **Server Key**: Encrypted in build config
- **Client**: Retrofit 2.9.0 with OkHttp 4.12.0

### Authentication
- **Token Management**: JWT-based token refresh
- **Interceptor**: Custom interceptor for request signing and header injection
- **Refresh Token Flow**: Automatic token refresh on 401 responses

### Networking Features
- **Request/Response Logging**: Chucker for debug builds
- **Serialization**: Moshi + Kotlinx Serialization
- **Error Handling**: Custom error models and exception handling
- **Retry Logic**: OkHttp interceptor with retry policies

### Network Security
- **HTTPS/TLS**: Network security configuration in `res/xml/network_security_config.xml`
- **Certificate Pinning**: Configured for production endpoints
- **Cleartext Traffic**: Limited to localhost and development (configurable)

### API Interfaces
Located in `core/api/interfaceDetail/`:
- User authentication endpoints
- Transaction management endpoints
- Drug master data endpoints
- Report generation endpoints
- HL7 message endpoints (where applicable)

---

## 💾 Database

### Room Database Architecture
- **Database Name**: AppDatabase
- **Version**: 1
- **Export Schema**: Enabled for migration tracking

### Entities

**1. UserEntity**
```
- User credentials (encrypted)
- User profile information
- Role and permissions
- Session details
```

**2. DrugMasterEntity**
```
- Drug identification (NDC codes)
- Drug name and description
- Dosage information
- Expiration tracking
```

**3. PillCountTxnEntity** (Transaction Header)
```
- Transaction ID
- User reference
- Transaction timestamp
- Status (pending, completed, synced)
- Transaction metadata
```

**4. PillCountTxnDetailsEntity** (Transaction Details)
```
- Detail ID
- Parent transaction reference
- Pill count result
- Item/batch information
- Barcode data
- Detection confidence
```

### DAOs (Data Access Objects)
Located in `core/room/dao/`:
- `UserDao`: User CRUD operations
- `DrugMasterDao`: Drug master data access
- `PillCountTxnDao`: Transaction header operations
- `PillCountTxnDetailsDao`: Transaction details operations

### Type Converters
- **SecureStringConverter**: Encrypts/decrypts sensitive strings using AES

### Migrations
Future schema changes require migration implementations:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migration logic
    }
}
```

---

## 🔐 Security

### Data Encryption
- **AES Encryption**: Used for sensitive data in database
- **Secure String Converter**: Room type converter for encrypted storage
- **Model Encryption**: TensorFlow Lite model is encrypted at rest

### TensorFlow Model Security
- **Model File**: `modelpilldetection` (encrypted)
- **Decryption**: `ModelDecryptor` utility decrypts model on-device
- **Usage**: Pre-loaded on app startup to Application scope
- **Location**: `/assets/` directory

### Network Security
- **HTTPS/TLS**: Enforced for all API communication
- **Network Security Config**: Defined in `res/xml/network_security_config.xml`
- **Certificate Pinning**: Enabled for critical endpoints

### Authentication Security
- **PIN Verification**: User PIN verification for sensitive operations
- **Token Management**: JWT tokens with automatic refresh
- **Session Management**: Secure session handling with timeout

### Permissions
- **Runtime Permissions**: Requested at runtime (Camera, Location, Notifications)
- **Permission Declarations**: Defined in `AndroidManifest.xml`
- **Scoped Storage**: Support for Android 11+ scoped storage

### File Protection
- **FileProvider**: Secure file URI generation
- **Backup Rules**: Custom backup configuration (`res/xml/backup_rules.xml`)
- **Data Extraction**: Defined rules for app data extraction

---

## 🧪 Testing

### Unit Testing
Located in `app/src/test/`:
- **Framework**: JUnit 4
- **Mocking**: Mockito, MockK
- **Coroutines**: kotlinx-coroutines-test
- **Reactive Testing**: Turbine for Flow testing

### Instrumentation Testing
Located in `app/src/androidTest/`:
- **Framework**: Espresso 3.5.1
- **Compose Testing**: Jetpack Compose UI test framework
- **Navigation Testing**: Navigation component testing utilities

### Running Tests
```bash
# Unit tests
./gradlew :app:testDebug

# Instrumentation tests
./gradlew :app:connectedAndroidTest

# Full test suite
./gradlew :app:test :app:connectedAndroidTest
```

### Test Coverage
- **Tool**: Jacoco (via enableUnitTestCoverage in debug builds)
- **Command**: `./gradlew :app:jacocoTestDebugUnitTestReport`

---

## 🆘 Troubleshooting

### Build Issues

**Problem**: Gradle sync fails
- **Solution**: 
  ```bash
  ./gradlew --refresh-dependencies
  ./gradlew clean build
  ```

**Problem**: Kotlin compilation errors
- **Solution**: Ensure JDK 11 is set in project structure

**Problem**: KAPT annotation processor errors
- **Solution**: Clean build and rebuild project

### Runtime Issues

**Problem**: TensorFlow model fails to load
- **Solution**: 
  - Verify `modelpilldetection` file exists in assets
  - Check model encryption/decryption logic
  - Verify sufficient memory available

**Problem**: Firebase FCM not receiving messages
- **Solution**:
  - Verify `google-services.json` is properly configured
  - Check device has Google Play Services
  - Verify FCM token is registered with backend

**Problem**: HL7 service not connecting
- **Solution**:
  - Verify network connectivity and permissions
  - Check HL7 server endpoint configuration
  - Review MLLP connection timeout settings

**Problem**: Database migration errors
- **Solution**:
  - Clear app data: `adb shell pm clear com.rite.pillcounting`
  - Reinstall app

### Performance Issues

**Problem**: Slow pill detection
- **Solution**:
  - Verify GPU delegate is active (check logs)
  - Reduce input image resolution if needed
  - Check for CPU bottlenecks in profiler

**Problem**: High memory usage
- **Solution**:
  - Monitor interpreter memory with profiler
  - Implement model unloading for background state
  - Use ProGuard for release builds

---

## 📝 Building & Contributing

### Code Style
- **Language**: Kotlin best practices
- **Naming**: camelCase for variables/methods, PascalCase for classes
- **Architecture**: Follow MVVM and Clean Architecture principles

### Pull Request Process
1. Create feature branch from develop
2. Implement feature with proper tests
3. Ensure all tests pass: `./gradlew test connectedAndroidTest`
4. Submit PR with description
5. Address code review comments

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

Examples:
- `feat(pillDetection): add GPU acceleration for model inference`
- `fix(api): handle token refresh correctly`
- `docs: update README with new features`

---

## 📦 Dependencies Summary

### Critical Dependencies
- **Android**: androidx-core, androidx-activity, androidx-lifecycle, androidx-room, androidx-navigation, androidx-compose
- **Networking**: retrofit, okhttp, moshi
- **DI**: dagger-hilt
- **ML**: tensorflow-lite, mlkit
- **Security**: bouncycastle, androidx-security-crypto
- **Firebase**: firebase-messaging, firebase-analytics

### Optional/Debug Dependencies
- **Debugging**: chucker (debug only)
- **Testing**: junit, mockito, espresso, turbine

### Version Management
All versions are defined in `build.gradle.kts` files. Use Gradle BOM (Bill of Materials) for version alignment:
- Compose BOM: 2024.06.00
- Firebase BOM: 33.2.0

---

## 📞 Support & Contact

For issues, questions, or contributions:
- **Project**: RITE Pill Counting Application
- **Organization**: Rite Technologies
- **Developed By**: Rite Technologies

---

For detailed documentation on specific features, refer to inline code comments and feature-specific README files in respective modules.

