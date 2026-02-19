import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * GuessTheNumber - A console-based number guessing game with multiple difficulty levels,
 * close-guess hints, and persistent high score tracking.
 *
 * The player selects a difficulty, then tries to guess a randomly chosen number
 * within a limited number of attempts. High scores (fewest tries to win) are
 * saved to a file so players can try to beat their personal bests.
 */
public class GuessTheNumber {

    /**
     * How close a guess must be to the target to trigger a "CLOSE!" hint.
     * For example, if the threshold is 5 and the target is 42, guesses
     * in the range 37-47 (exclusive of 42) will get the close hint.
     */
    private static final int CLOSE_THRESHOLD = 5;

    /** File path where high scores are persisted between sessions. */
    private static final String HIGH_SCORE_FILE = "highscores.txt";

    /** Shared Random instance used for generating the secret number. */
    private static final Random random = new Random();

    /** Shared Scanner instance for all user input. */
    private static final Scanner scanner = new Scanner(System.in);

    // ──────────────────────────────────────────────────────────────────────
    // Difficulty Enum
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Represents the available difficulty levels.
     * Each level defines an upper boundary for the random number range (1 to upperBound)
     * and the maximum number of guesses the player is allowed.
     *
     * The tries are roughly calibrated so that optimal binary-search play can win:
     *   Easy       1-50   in 10 tries  (very forgiving)
     *   Medium     1-100  in  7 tries  (comfortable)
     *   Hard       1-500  in  9 tries  (tight but possible)
     *   Impossible 1-1000 in 10 tries  (barely possible with perfect play)
     */
    enum Difficulty {
        EASY(50, 10, "Easy! Well, chickens are yellow!"),
        MEDIUM(100, 7, "Medium difficulty. A wise choice I think!"),
        HARD(500, 9, "Hard mode! Ok, let's play!"),
        IMPOSSIBLE(1000, 10, "Impossible mode. Brave choice... or stupid choice!");

        final int upperBound;
        final int maxTries;
        final String flavourText;

        Difficulty(int upperBound, int maxTries, String flavourText) {
            this.upperBound = upperBound;
            this.maxTries = maxTries;
            this.flavourText = flavourText;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Main
    // ──────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("Welcome to the Guess the Number Game!");

        // Load any previously saved high scores from disk
        Map<Difficulty, Integer> highScores = loadHighScores();

        // Outer loop: lets the player replay without restarting the program
        boolean playing = true;
        while (playing) {
            // Step 1 – Difficulty selection (returns null if the player chose to exit)
            Difficulty difficulty = selectDifficulty();
            if (difficulty == null) {
                // Player selected "Exit" from the menu
                System.out.println("Thanks for playing! Goodbye.");
                break;
            }

            // Step 2 – Play one round and capture the result (number of tries, or -1 for a loss)
            int tries = playGame(difficulty);

            // Step 3 – Show the outcome and update high scores if the player won
            if (tries > 0) {
                updateHighScore(highScores, difficulty, tries);
            }

            // Step 4 – Ask if the player wants to go again
            playing = askPlayAgain();
        }

        scanner.close();
    }

    // ──────────────────────────────────────────────────────────────────────
    // Difficulty Selection
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Displays the difficulty menu and waits for a valid choice.
     *
     * @return the chosen Difficulty, or null if the player selected "Exit"
     */
    private static Difficulty selectDifficulty() {
        while (true) {
            // Print the menu
            System.out.println();
            System.out.println("===== Pick your difficulty level =====");
            System.out.println("1. Easy");
            System.out.println("2. Medium");
            System.out.println("3. Hard");
            System.out.println("4. Impossible");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        System.out.println(Difficulty.EASY.flavourText);
                        return Difficulty.EASY;
                    case 2:
                        System.out.println(Difficulty.MEDIUM.flavourText);
                        return Difficulty.MEDIUM;
                    case 3:
                        System.out.println(Difficulty.HARD.flavourText);
                        return Difficulty.HARD;
                    case 4:
                        System.out.println(Difficulty.IMPOSSIBLE.flavourText);
                        return Difficulty.IMPOSSIBLE;
                    case 5:
                        // Signal the caller that the player wants to quit
                        return null;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                }
            } catch (InputMismatchException e) {
                // The user typed something that isn't an integer (e.g. "abc")
                System.out.println("Error: Please enter a valid integer.");
                scanner.next(); // Consume the bad token so the scanner can move on
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Core Game Loop
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Runs a single round of the guessing game for the given difficulty.
     *
     * @param difficulty the chosen difficulty level
     * @return the number of tries it took to win, or -1 if the player lost
     */
    private static int playGame(Difficulty difficulty) {
        // Generate a random target number in the range [1, upperBound]
        int numberToGuess = random.nextInt(difficulty.upperBound) + 1;

        System.out.printf("%nI'm thinking of a number between 1 and %d.%n", difficulty.upperBound);
        System.out.printf("You have %d attempts. Good luck!%n%n", difficulty.maxTries);

        int numberOfTries = 0;

        while (numberOfTries < difficulty.maxTries) {
            numberOfTries++;

            // Calculate and display remaining attempts
            int remaining = difficulty.maxTries - numberOfTries;

            // Warn the player when they're on their final guess
            if (remaining == 0) {
                System.out.println("** THIS IS YOUR LAST ATTEMPT! GUESS WISELY! **");
            }

            System.out.printf("(Attempt %d | %d remaining) Enter your guess: ",
                    numberOfTries, remaining);

            // Read the player's guess, handling non-integer input gracefully
            int guess = readInt();
            if (guess == Integer.MIN_VALUE) {
                // readInt() returns MIN_VALUE when the input wasn't a valid integer.
                // Don't count this as a used attempt.
                numberOfTries--;
                continue;
            }

            // Evaluate the guess and print feedback
            if (guess == numberToGuess) {
                // Correct guess – the player wins!
                System.out.printf("\t(Attempt %d): Congratulations! \uD83D\uDE0C "
                        + "You guessed the correct number in %d %s.%n",
                        numberOfTries, numberOfTries, numberOfTries == 1 ? "try" : "tries");
                return numberOfTries;
            }

            // Determine direction and closeness
            if (guess < numberToGuess) {
                if (guess + CLOSE_THRESHOLD >= numberToGuess) {
                    // Within the threshold – give an encouraging "close" hint
                    System.out.printf("\t(Attempt %d): CLOSE! But too low! Try again.%n", numberOfTries);
                } else {
                    System.out.printf("\t(Attempt %d): Too low! Try again.%n", numberOfTries);
                }
            } else {
                if (guess - CLOSE_THRESHOLD <= numberToGuess) {
                    System.out.printf("\t(Attempt %d): CLOSE! But too high! Try again.%n", numberOfTries);
                } else {
                    System.out.printf("\t(Attempt %d): Too high! Try again.%n", numberOfTries);
                }
            }
        }

        // The player used all their attempts without guessing correctly
        displayLoss(numberToGuess, difficulty);
        return -1;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Result Display
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Displays a loss message including the correct answer and how far the
     * player's last guess was from the target (if applicable).
     *
     * @param numberToGuess the secret number the player failed to guess
     * @param difficulty    the difficulty that was being played
     */
    private static void displayLoss(int numberToGuess, Difficulty difficulty) {
        System.out.printf("%nBetter luck next time. The correct number was %d :-(", numberToGuess);
        System.out.printf("%nThe number was %.1f%% of the way through the range 1-%d.%n",
                (numberToGuess / (double) difficulty.upperBound) * 100, difficulty.upperBound);
    }

    // ──────────────────────────────────────────────────────────────────────
    // High Score Management
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Loads high scores from the HIGH_SCORE_FILE.
     * The file format is one line per difficulty: "DIFFICULTY_NAME score".
     * If the file doesn't exist or can't be read, returns an empty map.
     *
     * @return a map from Difficulty to the best (lowest) number of tries
     */
    private static Map<Difficulty, Integer> loadHighScores() {
        Map<Difficulty, Integer> scores = new HashMap<>();
        File file = new File(HIGH_SCORE_FILE);

        if (!file.exists()) {
            return scores;
        }

        // Read each line and parse "DIFFICULTY_NAME score"
        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    try {
                        Difficulty diff = Difficulty.valueOf(parts[0]);
                        int score = Integer.parseInt(parts[1]);
                        scores.put(diff, score);
                    } catch (IllegalArgumentException e) {
                        // Skip malformed lines (unknown difficulty or non-integer score)
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Note: Could not load high scores. Starting fresh.");
        }

        return scores;
    }

    /**
     * Saves the high score map to disk so scores persist between sessions.
     *
     * @param scores the current high score map
     */
    private static void saveHighScores(Map<Difficulty, Integer> scores) {
        try (FileWriter writer = new FileWriter(HIGH_SCORE_FILE)) {
            for (Map.Entry<Difficulty, Integer> entry : scores.entrySet()) {
                writer.write(entry.getKey().name() + " " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not save high scores.");
        }
    }

    /**
     * Checks whether the player's result is a new high score for the given
     * difficulty. If so, updates the map and saves to disk.
     *
     * @param scores     the current high score map (may be mutated)
     * @param difficulty the difficulty that was played
     * @param tries      the number of tries it took the player to win
     */
    private static void updateHighScore(Map<Difficulty, Integer> scores, Difficulty difficulty, int tries) {
        Integer previousBest = scores.get(difficulty);

        if (previousBest == null || tries < previousBest) {
            // New record!
            scores.put(difficulty, tries);
            saveHighScores(scores);

            if (previousBest == null) {
                System.out.printf("New high score for %s: %d %s!%n",
                        difficulty.name(), tries, tries == 1 ? "try" : "tries");
            } else {
                System.out.printf("New high score for %s! %d %s (previous best: %d)%n",
                        difficulty.name(), tries, tries == 1 ? "try" : "tries", previousBest);
            }
        } else {
            // Didn't beat the record – show the current best for motivation
            System.out.printf("Your high score for %s is %d %s. Keep trying!%n",
                    difficulty.name(), previousBest, previousBest == 1 ? "try" : "tries");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Play Again Prompt
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Asks the player whether they'd like to play another round.
     *
     * @return true if the player wants to play again, false otherwise
     */
    private static boolean askPlayAgain() {
        System.out.print("\nWould you like to play again? (y/n): ");

        // Read the answer; default to "no" on unexpected input
        String answer = scanner.next().trim().toLowerCase();
        return answer.startsWith("y");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Input Helpers
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Safely reads an integer from the scanner. If the user types something
     * that isn't a valid integer, prints an error and returns Integer.MIN_VALUE
     * as a sentinel so the caller can decide how to handle the bad input.
     *
     * @return the integer the user entered, or Integer.MIN_VALUE on bad input
     */
    private static int readInt() {
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Error: Please enter a valid integer.");
            scanner.next(); // Consume the invalid token to prevent an infinite loop
            return Integer.MIN_VALUE;
        }
    }
}
