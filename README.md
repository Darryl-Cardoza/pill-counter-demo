# MobRite Pill Counting Application - Android

> **An advanced Android application for pharmaceutical pill counting using AI, barcode scanning, and HL7 integration for healthcare systems.** 

---

## 📚 Table of Contents

* [✨ What's Included](#-whats-included)
* [🛠️ Tech Stack](#️-tech-stack)
* [📋 Prerequisites](#-prerequisites)
* [🚀 Project Setup (Step by Step)](#-project-setup-step-by-step)
* [🏃 Running the Project](#-running-the-project)
* [🧪 Testing](#-testing)
* [📁 Folder Structure Overview](#-folder-structure-overview)
* [⚙️ Configuration Details](#️-configuration-details)
* [📦 Adding a New Module](#-adding-a-new-module)

---

## ✨ What's Included

* AI-powered pill detection using TensorFlow Lite (on-device)
* Barcode scanning via ML Kit
* HL7 healthcare system integration (MLLP + NSD)
* Transaction management with history & sync
* Secure authentication with PIN & token lifecycle
* AES encryption for sensitive data
* Firebase Cloud Messaging (FCM) notifications
* Dashboard analytics & reporting
* Location tracking (GPS integration)
* Fully modular Clean Architecture (MVVM)

---

## 🛠️ Tech Stack

| Category      | Technology                |
| ------------- | ------------------------- |
| Platform      | Android (Jetpack Compose) |
| Language      | Kotlin                    |
| Architecture  | MVVM + Clean Architecture |
| DI            | Hilt                      |
| Networking    | Retrofit + OkHttp         |
| Database      | Room (SQLite)             |
| AI/ML         | TensorFlow Lite + ML Kit  |
| Security      | AES + BouncyCastle        |
| Notifications | Firebase FCM              |
| Async         | Coroutines + Flow         |

---

## 📋 Prerequisites

* Android Studio (latest)
* Android SDK 36
* JDK 11+
* Gradle 8+
* Firebase Project setup
* Physical device / Emulator

---

## 🚀 Project Setup (Step by Step)

### 1. Clone Repository

```bash
git clone <repository-url>
cd pill-counter-android
```

### 2. Configure Local Properties

```properties
sdk.dir=/path/to/android/sdk
```

### 3. Firebase Setup

* Add `google-services.json` in `app/`
* Enable Firebase Cloud Messaging

### 4. Build Project

```bash
./gradlew :app:assembleDebug
```

---

## 🏃 Running the Project

```bash
./gradlew :app:installDebug
adb shell am start -n com.rite.pillcounting/.MainActivity
```

---

## 🧪 Testing

### Run Unit Tests

```bash
./gradlew :app:testDebug
```

### Run Instrumentation Tests

```bash
./gradlew :app:connectedAndroidTest
```

### Coverage Report

```bash
./gradlew :app:jacocoTestDebugUnitTestReport
```

---

## 📁 Folder Structure Overview

```
app/
├── core/                  # Infrastructure (API, DB, Security, Utils)
├── feature/               # Feature-based modules
│   ├── barcodeScan/
│   ├── pillCountScan/
│   ├── login/
│   ├── dashboard/
│   ├── history/
│   └── settings/
├── navigation/            # Navigation graphs
├── ui/                    # Theme & UI configs
└── MainActivity.kt
```

### Principles

* Feature-first architecture
* Clear separation of layers
* High modularity and scalability

---

## ⚙️ Configuration Details

### 🌐 API Configuration

* Base URL: `https://pill.ccrlindia.com/`
* Token-based authentication
* Automatic token refresh

---

### 💾 Database

* Room Database (SQLite)
* Entities:

  * User
  * Drug Master
  * Transactions
  * Transaction Details

---

### 🔐 Security

* AES encryption for sensitive data
* Encrypted TensorFlow models
* Certificate pinning for APIs
* Secure token/session management

---

### 🤖 AI / ML

* TensorFlow Lite model (GPU accelerated)
* On-device inference
* Encrypted model loading

---

## 📦 Adding a New Module

Follow Clean Architecture:

```
feature/
└── new_feature/
    ├── data/
    ├── domain/
    └── presentation/
```

### Steps

1. Create feature folder
2. Add:

   * Entity
   * Repository
   * UseCases
   * ViewModel
   * UI Screens
3. Add navigation route
4. Bind using Hilt

---
