import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Random;

public class GuessTheNumber {
    public static void main(String[] args) {
        final int withinFive = 5; // static variable

        Random random = new Random();
        Scanner scanner = new Scanner(System.in);

        int numberOfMaxTries = 5; // set the default to medium difficulty
        int numberOfTries = 0;
        int guess;
        int choice = 0;
        int upperBoundary = 100;  // set the default to medium difficulty
        boolean win = false;

        System.out.println("Welcome to the Guess the Number Game!");

        // Loop until the user chooses to exit
        while (choice < 1 || choice > 5 ) {
            // Display the menu options
            System.out.println("===== Pick your difficulty level =====");
            System.out.println("1. Option 1: Easy");
            System.out.println("2. Option 2: Medium");
            System.out.println("3. Option 3: Difficult");
            System.out.println("4. Option 4: Impossible");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            try {
                // Get user input
                choice = scanner.nextInt();

                // Perform actions based on the user's choice
                switch (choice) {
                    case 1: // easy
                        upperBoundary = 50;
                        numberOfMaxTries = 10;
                        System.out.println("Easy!  Well, chickens are yellow!");
                        break;
                    case 2: // medium (also default)
                        upperBoundary = 100;
                        // numberOfMaxTries = 5; // leave this as the default
                        System.out.println("Medium difficulty.  A wise choice I think!");
                        break;
                    case 3: // hard
                        upperBoundary = 500;
                        // numberOfMaxTries = 5; // leave this as the default
                        System.out.println("Hard mode! Ok, let's play!");
                        break;
                    case 4: // impossible
                        upperBoundary = 1000;
                        // numberOfMaxTries = 5; // leave this as the default
                        System.out.println("Impossible mode.  Brave choice... or stupid choice!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                // Handle the error when the input is not an integer
                System.out.println("Error: Please enter a valid integer.");
                scanner.next(); // Consume the invalid input to avoid an infinite loop
                System.out.println(); // Add a blank line for better readability
            }
        }

        // Random number between 1 and whatever the upperBoundary is
        int numberToGuess = random.nextInt(upperBoundary) + 1;

        System.out.printf("I'm thinking of a number between 1 and %d.\n", upperBoundary);
        System.out.printf("You have %d attempts\n", numberOfMaxTries );

        while ( !win && numberOfTries < numberOfMaxTries ) {
            numberOfTries++;

            if ( numberOfTries == numberOfMaxTries ) {
                System.out.println("** THIS IS YOUR LAST ATTEMPT!  GUESS WISELY! ** ");
            }

            System.out.printf("(Attempt %d) Enter your guess: ", numberOfTries);
            guess = scanner.nextInt();
            String messageString = "";

            if ( guess == numberToGuess ) {
                win = true;
                System.out.printf("\t(Attempt %d): Congratulations! \uD83D\uDE0C You've guessed the correct number in %d tries.\n", numberOfTries, numberOfTries);
            } else if ( guess < numberToGuess && ( guess + withinFive ) >= numberToGuess ) {
                System.out.printf("\t(Attempt %d): CLOSE! But too low! Try again.\n", numberOfTries);
            } else if ( guess < numberToGuess ) {
                System.out.printf("\t(Attempt %d): Too low! Try again.\n", numberOfTries);
            } else if ( guess > numberToGuess && ( guess - withinFive ) <= numberToGuess ) {
                System.out.printf("\t(Attempt %d): CLOSE! But too high! Try again.\n", numberOfTries);
            } else {
                System.out.printf("\t(Attempt %d): Too high! Try again.\n", numberOfTries);
            }
        }
        scanner.close();

        if ( !win && numberOfTries == numberOfMaxTries){
            System.out.printf("Better luck next time.  The correct number was %d :-(\n", numberToGuess );
        }
    }
}