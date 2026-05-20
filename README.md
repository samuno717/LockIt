# 🔐 LockIt — Password Manager

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](#)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](#)
[![Room](https://img.shields.io/badge/Database-Room_SQLite-003B57?logo=sqlite&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#)

**LockIt** is a modern, offline-first password manager for Android. All credentials are stored locally on the device — no internet connection, no cloud, no risk. Built with Jetpack Compose, Room, and a clean MVVM architecture.

---

## ✨ Features

| Feature | Description |
| :--- | :--- |
| 🔒 **Secure Locker** | Store and manage account credentials (service, email, username, password, website) locally |
| 🗝️ **Master Passkey** | Single master password protects access to all stored entries |
| 🛠️ **PassTools** | Built-in password strength meter (`PassMeter`) and customizable password generator (`Generator`) |
| 🌓 **Dynamic UI** | Full Light and Dark mode support, toggled from Settings |
| 📴 **Privacy First** | 100% on-device storage — no network calls, no data sharing |
| 🌐 **Multilingual** | Interface available in English and Polish (`values/` + `values-pl/`) |
| 🔍 **Live Search** | Real-time filtering of vault entries by service name using `Flow` |

---

## 🏗️ System Architecture

LockIt follows **Clean Architecture** principles using the **MVVM (Model-View-ViewModel)** pattern. This provides a strict separation between UI, business logic, and data persistence.

```mermaid
graph TD
    subgraph UI_Layer ["UI Layer — Jetpack Compose"]
        A[MainActivity] --> B[Navigation Graph\nScreen.kt]
        B --> C[LoginScreen]
        B --> D[RegisterScreen]
        B --> E[LockerScreen]
        B --> F[PasswordDetailsScreen]
        B --> G[AddPasswordScreen]
        B --> H[PassToolsScreen]
        B --> I[PassMeterScreen]
        B --> J[GeneratorScreen]
        B --> K[SettingsScreen]
        B --> L[AccountScreen]
        B --> M[NotificationsScreen]
    end

    subgraph Logic_Layer ["State Management"]
        N[LockItViewModel]
    end

    subgraph Data_Layer ["Data Layer — Room SQLite"]
        O[LockItRepository]
        P[LockItDatabase]
        Q[LockItDao]
    end

    E & F & G & H & I & J & K & L <--> N
    C & D --> N
    N <--> O
    O <--> Q
    Q <--> P
```

### Architectural pillars

- **Single ViewModel** — `LockItViewModel` manages state for all screens, avoiding redundant repository calls
- **Reactive Streams** — `Kotlin Flow` and `StateFlow` for real-time UI updates; live search via `flatMapLatest`
- **Single Source of Truth** — Room database is the only source of data; UI only observes state
- **Declarative UI** — 100% Jetpack Compose, no XML layouts
- **Singleton DB** — `LockItDatabase` uses a thread-safe `@Volatile` singleton pattern

---

## 🗺️ Navigation Map

The app starts at **LoginScreen**, which gates access behind the master passkey. After authentication the user lands on **LockerScreen** — the central vault hub. From there, a bottom navigation bar gives access to **PassToolsScreen** and **SettingsScreen** at all times.

<p align="center">
  <img src="docs/navmap.jpg" alt="LockIt Navigation Map" width="900">
</p>

### Screen overview

| Screen | File | Accessed from | Description |
| :--- | :--- | :--- | :--- |
| `LoginScreen` | `LoginScreen.kt` | App launch | Prompts for master passkey; entry point on every launch. |
| `RegisterScreen` | `RegisterScreen.kt` | `LoginScreen` | First-launch setup — choose username and create master passkey. |
| `LockerScreen` | `LockerScreen.kt` | `LoginScreen` (after auth) | Main vault — categorised list of all entries, live search bar, FAB to add. |
| `PasswordDetailsScreen` | `PasswordDetailsScreen.kt` | `LockerScreen` (eye icon) | Full view of a single entry — shows email, password, website. |
| `AddPasswordScreen` | `AddPasswordScreen.kt` | `LockerScreen` (+ FAB) | Form to save a new credential (service, email, username, password, website, category). |
| `PassToolsScreen` | `PassToolsScreen.kt` | Bottom nav | Hub for password tools — links to PassMeter and Generator. |
| `PassMeterScreen` | `PassMeterScreen.kt` | `PassToolsScreen` | Type any password to get an instant strength score with a visual bar. |
| `GeneratorScreen` | `GeneratorScreen.kt` | `PassToolsScreen` | Generate a password with configurable length, A-Z, a-z, 0-9, symbols. |
| `SettingsScreen` | `SettingsScreen.kt` | Bottom nav | Account, Notifications, Language, Reset passkey, Dark mode toggle, Logout. |
| `AccountScreen` | `AccountScreen.kt` | `SettingsScreen` | View and edit user profile, change avatar image. |
| `NotificationsScreen` | `NotificationsScreen.kt` | `SettingsScreen` | In-app security alerts and notifications. |
| `AudioPlayerScreen` | `AudioPlayerScreen.kt` | Internal | Audio playback (Media3 / ExoPlayer). |
| `VideoPlayerScreen` | `VideoPlayerScreen.kt` | Internal | Video playback with hardware acceleration. |

---

## 🎨 Mock-ups

<p align="center">
  <img src="docs/mockup-login.png" alt="Login Screen" width="220"/>
  &nbsp;&nbsp;
  <img src="docs/mockup-locker.png" alt="Locker Screen" width="220"/>
  &nbsp;&nbsp;
  <img src="docs/mockup-details.png" alt="Password Details" width="220"/>
</p>

<p align="center">
  <img src="docs/mockup-add.png" alt="Add Password" width="220"/>
  &nbsp;&nbsp;
  <img src="docs/mockup-passtools.png" alt="PassTools" width="220"/>
  &nbsp;&nbsp;
  <img src="docs/mockup-settings.png" alt="Settings" width="220"/>
</p>

---

## 📊 Database Architecture

LockIt uses the **Room Persistence Library** (SQLite abstraction). The database is named `lockit_database`, version `1`, and contains two entities: `password_entries` and `users`.

### Entity Relationship Diagram

```mermaid
erDiagram
    users {
        Int id PK "Always 1 — singleton row"
        String username "Display name, default: User"
        String passkey "Master password (plain — see Security note)"
        String profileImage "URI to avatar image, nullable"
        Long createdAt "Account creation timestamp"
        Long lastLogin "Timestamp of last successful login"
    }

    password_entries {
        Long id PK "Auto-increment"
        String serviceName "Name of the service, e.g. Facebook"
        String email "Account email address"
        String username "Account username / login"
        String password "Account password"
        String website "Associated website URL"
        String category "Category tag: Vault, Games, Social Media, etc."
        Int iconRes "Resource ID of the service icon"
    }

    users ||--o{ password_entries : "owns"
```

### Data Schema — `password_entries`

| Field | Type | Constraint | Purpose |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `PRIMARY KEY, autoGenerate` | Unique row identifier |
| `serviceName` | `String` | `NOT NULL` | Name of the service (also used for live search) |
| `email` | `String` | `NOT NULL` | Email associated with the account |
| `username` | `String` | `NOT NULL` | Username / login for the account |
| `password` | `String` | `NOT NULL` | Account password |
| `website` | `String` | `NOT NULL` | Website URL for the service |
| `category` | `String` | `NOT NULL` | Category for tab-based filtering (e.g. Social Media) |
| `iconRes` | `Int` | `NOT NULL` | Drawable resource ID of the service icon |

### Data Schema — `users`

| Field | Type | Constraint | Purpose |
| :--- | :--- |:--- | :--- |
| `id` | `Int` | `PRIMARY KEY` | Always `1` — only one user row ever exists |
| `username` | `String` | `DEFAULT "User"` | Display name shown on the Settings screen |
| `passkey` | `String` | `NOT NULL` | Master passkey used to authenticate |
| `profileImage` | `String?` | `NULLABLE` | URI string pointing to the chosen avatar image |
| `createdAt` | `Long` | `NOT NULL` | Timestamp when the account was first created |
| `lastLogin` | `Long` | `NOT NULL` | Timestamp updated on every successful login |

---

## 🧠 ViewModel & Data Flow

LockIt uses a **single shared ViewModel** — `LockItViewModel` — injected via a custom `ViewModelProvider.Factory` that builds the dependency chain (`Database → DAO → Repository → ViewModel`) without a DI framework.

### `LockItViewModel` — State & Functions

| State / Function | Type | Description |
| :--- | :--- | :--- |
| `currentUser` | `StateFlow<User?>` | Currently logged-in user; `null` when not authenticated |
| `searchQuery` | `StateFlow<String>` | Current text in the search bar |
| `passwords` | `StateFlow<List<PasswordEntry>>` | Full list or filtered list — reacts to `searchQuery` via `flatMapLatest` |
| `isDarkMode` | `StateFlow<Boolean>` | Current theme state; toggled from SettingsScreen |
| `loadUser()` | `private suspend` | Called on `init`; loads user from DB and updates `lastLogin` |
| `registerUser(username, passkey)` | `suspend` | Creates a new `User` row and sets `currentUser` |
| `updateProfileImage(uri)` | `suspend` | Updates the user's avatar URI in DB and state |
| `addPassword(entry)` | `suspend` | Inserts a new `PasswordEntry` via repository |
| `deletePassword(entry)` | `suspend` | Deletes a `PasswordEntry` via repository |
| `updateSearchQuery(query)` | — | Updates `searchQuery`, which triggers `flatMapLatest` to requery |
| `toggleDarkMode(enabled)` | — | Flips `isDarkMode` state |
| `resetMasterKey()` | `suspend` | Clears `currentUser` to force re-authentication |

### Data flow — password list with live search

```
User types in search bar
        │
        ▼
updateSearchQuery(query)
        │
        ▼
_searchQuery (MutableStateFlow)
        │
        ▼  flatMapLatest
  query.isBlank?
   ├── YES → LockItDao.getAllPasswords()      → Flow<List<PasswordEntry>>
   └── NO  → LockItDao.searchPasswords(query) → Flow<List<PasswordEntry>>
        │
        ▼
  passwords (StateFlow) ──► LockerScreen UI (collectAsStateWithLifecycle)
```

### Data flow — DAO queries (`LockItDao`)

| Function | Return type | Description |
| :--- | :--- | :--- |
| `getUser()` | `suspend User?` | Fetches the single user row (`WHERE id = 1`) |
| `insertUser(user)` | `suspend` | Insert or replace user (handles both register and profile update) |
| `getAllPasswords()` | `Flow<List<PasswordEntry>>` | Reactive stream of all entries — emits on every DB change |
| `insertPassword(entry)` | `suspend` | Insert or replace a password entry |
| `deletePassword(entry)` | `suspend` | Delete a specific entry |
| `getPasswordById(id)` | `suspend PasswordEntry?` | Fetch one entry by its ID |
| `searchPasswords(query)` | `Flow<List<PasswordEntry>>` | Reactive search by `serviceName LIKE '%query%'` |

---

## 🔐 Security

LockIt stores all data locally on-device. No data is transmitted over the network.

| Aspect | Implementation |
| :--- | :--- |
| **Storage** | All entries saved to Room SQLite database on-device only |
| **Master passkey** | Stored in the `users` table; app validates on every launch |
| **Session** | `currentUser` state is held in memory — cleared on logout or `resetMasterKey()` |
| **Privacy** | No analytics, no network permissions, no cloud sync |
| **`lastLogin`** | Updated in DB on every successful authentication via `loadUser()` |

> 💡 **Note:** The current implementation stores `passkey` as plain text in the database. A recommended next step would be to hash it using `PBKDF2-HMAC-SHA256` with a random salt before persisting.

---

## 🛠️ Technology Stack

| Category | Library / Tool | Version |
| :--- | :--- | :--- |
| Language | Kotlin | 1.9+ |
| UI | Jetpack Compose | BOM 2024.x |
| Architecture | MVVM | — |
| Database | Room (SQLite) — `lockit_database` v1 | 2.6+ |
| Async | Coroutines + Flow + StateFlow + `flatMapLatest` | — |
| Image Loading | Coil | 2.x |
| Media | Media3 / ExoPlayer | 1.x |
| Navigation | Compose Navigation (routes defined in `Screen.kt`) | 2.7+ |
| Build | Gradle KTS + Version Catalog (`libs.versions.toml`) | 8.x |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug or newer
- **JDK 17**
- **Android SDK** 26+ (target: 34)

### Build & Run
```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/LockIt.git

# 2. Open in Android Studio → File → Open → select the project folder

# 3. Let Gradle sync (first time may take a few minutes)

# 4. Run on emulator or physical device (▶ button)
```

### First Launch
On the first run, LockIt redirects to **RegisterScreen** where you create a username and master passkey. This creates the singleton `User` row (`id = 1`) in the database. Every subsequent launch goes through **LoginScreen**, which reads that row and validates the entered passkey.

---

## 📁 Project Structure

```
LockIt/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/example/lockit/
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── LockItDao.kt           # All Room queries (DAO interface)
│       │   │   │   │   ├── LockItDatabase.kt      # Room DB definition, singleton factory
│       │   │   │   │   └── LockItRepository.kt    # Data access layer between ViewModel and DAO
│       │   │   │   └── model/
│       │   │   │       ├── PasswordEntry.kt       # @Entity — table: password_entries
│       │   │   │       └── User.kt                # @Entity — table: users
│       │   │   ├── ui/
│       │   │   │   ├── screens/
│       │   │   │   │   ├── LoginScreen.kt
│       │   │   │   │   ├── RegisterScreen.kt
│       │   │   │   │   ├── LockerScreen.kt
│       │   │   │   │   ├── AccountScreen.kt
│       │   │   │   │   ├── AddPasswordScreen.kt
│       │   │   │   │   ├── PasswordDetailsScreen.kt
│       │   │   │   │   ├── PassToolsScreen.kt
│       │   │   │   │   ├── GeneratorScreen.kt
│       │   │   │   │   ├── PassMeterScreen.kt
│       │   │   │   │   ├── NotificationsScreen.kt
│       │   │   │   │   ├── AudioPlayerScreen.kt
│       │   │   │   │   ├── VideoPlayerScreen.kt
│       │   │   │   │   └── SettingsScreen.kt
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt               # Color palette (Light + Dark)
│       │   │   │       ├── Theme.kt               # MaterialTheme wrapper
│       │   │   │       ├── Type.kt                # Typography definitions
│       │   │   │       └── Screen.kt              # Navigation route constants
│       │   │   ├── viewmodel/
│       │   │   │   └── LockItViewModel.kt         # Single shared ViewModel + Factory
│       │   │   └── MainActivity.kt
│       │   └── res/
│       │       ├── drawable/                      # App icons (XML vectors)
│       │       ├── mipmap-*/                      # Launcher icons (all screen densities)
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml                # English strings
│       │       │   └── themes.xml
│       │       ├── values-pl/
│       │       │   └── strings.xml                # Polish strings
│       │       └── xml/
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       ├── androidTest/                           # Instrumented tests
│       └── test/                                  # Unit tests
├── gradle/
│   └── wrapper/
│       └── libs.versions.toml                     # Dependency version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

