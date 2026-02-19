# 🎯 Guess The Number

A fun, console-based number guessing game built in Java! Pick a difficulty, and try to guess the secret number before you run out of attempts.

## 🎮 How To Play

1. Run the program
2. Choose a difficulty level from the menu
3. The computer picks a random number within the difficulty's range
4. Enter your guesses — you'll get hints telling you if you're too high, too low, or **CLOSE!** 🔥
5. Try to guess the number before your attempts run out
6. Beat your high score and play again!

## 📊 Difficulty Levels

| Level | Range | Attempts | Description |
|-------|-------|----------|-------------|
| 🟢 Easy | 1 – 50 | 10 | Very forgiving — great for beginners |
| 🟡 Medium | 1 – 100 | 7 | A comfortable challenge |
| 🔴 Hard | 1 – 500 | 9 | Tight but possible with smart play |
| 💀 Impossible | 1 – 1,000 | 10 | Barely possible — even with perfect strategy |

## ✨ Features

- **🎚️ Four difficulty levels** — from casual to punishing
- **🔥 Close-guess hints** — get a special "CLOSE!" message when you're within 5 of the answer
- **📈 Remaining attempts counter** — always know how many guesses you have left
- **🏆 Persistent high scores** — your best results are saved to `highscores.txt` and loaded automatically
- **🔁 Play again loop** — no need to restart the program between rounds
- **🛡️ Input validation** — non-integer input is handled gracefully without crashing
- **📍 Loss insights** — when you lose, see where the number fell in the range as a percentage

## 🚀 Getting Started

### Prerequisites

- Java 8 or later (JDK)

### Compile & Run

```bash
# Compile
javac -d out src/GuessTheNumber.java

# Run
java -cp out GuessTheNumber
```

### Using an IDE

Open the project in IntelliJ IDEA (or your preferred Java IDE) and run `GuessTheNumber.main()`.

## 📂 Project Structure

```
GuessTheNumber/
├── src/
│   └── GuessTheNumber.java   # All game logic
├── .gitignore
├── GuessTheNumber.iml         # IntelliJ project file
└── README.md
```

## 🧠 Strategy Tips

- Use **binary search** — always guess the midpoint of the remaining range
- Pay attention to the **"CLOSE!"** hints — they mean you're within 5 of the answer
- On Impossible mode, you need near-perfect binary search to win (10 tries for 1–1,000 = exactly `⌈log₂(1000)⌉`)

## 📝 License

This project is open source and available for personal and educational use.
