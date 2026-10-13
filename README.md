# Anagram Tiles 🔠

*“Shuffle letters. Find the word.”*

**Anagram Tiles** is a modern, fast-paced Android word game built with Kotlin and Jetpack Compose. Test your vocabulary and puzzle-solving skills against an AI opponent or play at your own pace offline!

---

## 🌟 Key Features

- 🔤 **Dynamic Anagram Puzzle Engine**: Instant puzzle generation powered by a bundled local dictionary (`word_list.txt` and `anagram_words.txt`).
- 🤖 **Smart AI Opponent**: Battle CPU players across 3 distinct difficulty settings:
  - **Easy**: Solves common, shorter anagrams at a relaxed pace.
  - **Normal**: Balanced speed and word selection.
  - **Hard**: Aggressive dictionary search targeting longer and high-scoring words.
- ⏱️ **Flexible Game Timers**: Choose between 30s, 60s, 90s per round, or play in relaxed mode with the timer turned off.
- 🎨 **Modern Jetpack Compose & Material 3**: Sleek tile interactive animations, full dark mode support, and fluid layout responsiveness.
- 🔊 **Sound & Haptic Feedback**: Interactive tile tap sounds, error alerts, success chimes, and haptic engine integration.
- 📊 **Detailed Game Statistics**: Track your games played, win streak, best puzzle score, longest word found, and lifetime performance.
- 🔒 **100% Offline & Private**:
  - No internet connection required.
  - No user tracking, ads, or analytics.
  - No account creation or paywalls.

---

## 🎮 How to Play

1. **Form Words**: Tap letter tiles to select and build words. Swap or re-order tiles to spot hidden anagrams.
2. **Submit Answers**: Submit valid words from the provided tile set before time runs out.
3. **Outscore the CPU**: Earn points for longer words and consecutive anagram discoveries in the same puzzle.

### 💯 Scoring Rules

| Word Length | Base Points |
| :--- | :--- |
| **3 letters** | 20 pts |
| **4 letters** | 30 pts |
| **5 letters** | 45 pts |
| **6 letters** | 60 pts |
| **7 letters** | 80 pts |
| **8+ letters** | 100 pts |

#### Anagram Bonuses
- **1st Anagram Found**: Base score
- **Subsequent Anagrams**: Additional bonus (`+5` for 2nd, `+10` for 3rd, `+15` for 4th, etc.)
- **All Anagrams Found Bonus**: Extra **+25 points** completion bonus!

---

## 🏗️ Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: Clean Architecture (Data, Domain, UI) with Unidirectional Data Flow (UDF)
- **State Management**: Android `ViewModel`, `StateFlow`
- **Navigation**: Navigation Compose
- **Persistence**: Local Preferences with JSON serialization
- **Target SDK**: 37 (Minimum SDK 26 — Android 8.0+)

### Project Structure

```text
com.example.anagramtiles/
├── data/           # Models, Preferences, Dictionary loaders, Enums
├── domain/         # Game Engine, CPU Strategy, Scoring Logic, Puzzle Generator
├── ui/
│   ├── audio/      # SoundPool & Haptic Feedback Controller
│   ├── components/ # Letter Tiles, Scoreboards, Bottom Nav, Sheets
│   ├── navigation/ # Compose Navigation Graph & Screen routes
│   ├── screens/    # Home, Game, Result, Settings, About, Splash
│   ├── theme/      # Color Palette, Typography, Material 3 Theme
│   └── viewmodel/  # AnagramViewModel & UI State handling
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug or newer recommended)
- **JDK 11** or higher
- **Android SDK 37** (or compatible SDK version installed)

### Building & Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Anagram-Tiles.git
   cd Anagram-Tiles
   ```

2. **Open in Android Studio**:
   - Open Android Studio, click **File > Open**, and select the project directory.

3. **Build via Command Line**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 🧪 Testing

The codebase includes test suites for core game engine mechanics, CPU strategies, dictionary parsing, tile manipulation, and scoring algorithms:
- `GameEngineAndFlowTest.kt`
- `CpuStrategyTest.kt`
- `ValidationAndScoringTest.kt`
- `TileManipulationTest.kt`
- `DictionaryAndAnagramTest.kt`

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
