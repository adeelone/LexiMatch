package com.leximatch.view;

import com.leximatch.model.GameDifficulty;
import com.leximatch.model.GameSession;
import com.leximatch.model.GuessEvaluation;
import com.leximatch.model.LetterResult;
import com.leximatch.model.LetterStatus;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class ConsoleView {
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleView(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public void showWelcome(int dictionarySize, int answerBankSize) {
        out.println("LexiMatch");
        out.println("---------");
        out.println("Guess the hidden five-letter word.");
        out.println("Dictionary loaded: " + dictionarySize + " valid guesses.");
        out.println("Answer bank loaded: " + answerBankSize + " possible solutions.");
        out.println("Type 'quit' at any guess prompt to exit.");
        out.println();
    }

    public GameDifficulty promptDifficulty() {
        while (true) {
            out.println("Choose a difficulty:");
            GameDifficulty[] values = GameDifficulty.values();
            for (int index = 0; index < values.length; index++) {
                GameDifficulty difficulty = values[index];
                out.printf("%d. %s (%d attempts)%n", index + 1, difficulty.label(), difficulty.maxAttempts());
            }
            out.print("> ");

            if (!scanner.hasNextLine()) {
                out.println();
                return null;
            }
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= values.length) {
                    out.println();
                    return values[choice - 1];
                }
            } catch (NumberFormatException ignored) {
            }
            out.println("Please enter a number between 1 and 4.");
        }
    }

    public void showDifficultyRules(GameDifficulty difficulty) {
        out.println("Mode: " + difficulty.label());
        if (difficulty.hardMode()) {
            out.println("Hard mode is enabled: confirmed clues must be reused.");
        }
        if (difficulty.revealHint()) {
            out.println("Beginner mode gives a first-letter hint after 3 guesses.");
        }
        out.println();
    }

    public void showBoard(GameSession session) {
        out.printf("Attempts: %d/%d%n", session.getAttemptsUsed(), session.getDifficulty().maxAttempts());
        List<GuessEvaluation> history = session.getHistory();
        if (history.isEmpty()) {
            out.println("Board is empty. Start with any five-letter word.");
        } else {
            for (GuessEvaluation evaluation : history) {
                out.println(renderEvaluation(evaluation));
            }
        }
        if (!session.getHint().isEmpty()) {
            out.println(session.getHint());
        }
        out.println();
    }

    public String promptGuess(GameSession session) {
        out.printf("Enter guess (%d remaining): ", session.getAttemptsRemaining());
        if (!scanner.hasNextLine()) {
            out.println();
            return null;
        }
        return scanner.nextLine();
    }

    public void showEvaluation(GuessEvaluation evaluation) {
        if (!evaluation.valid()) {
            out.println(evaluation.message());
        } else {
            out.println(renderEvaluation(evaluation));
        }
        out.println();
    }

    public void showGameResult(GameSession session) {
        if (session.isWon()) {
            out.println("You solved it. The word was " + session.getTargetWord().toUpperCase(Locale.ROOT) + ".");
        } else {
            out.println("Out of attempts. The word was " + session.getTargetWord().toUpperCase(Locale.ROOT) + ".");
        }
        out.println();
    }

    public boolean promptReplay() {
        out.print("Play again? (y/n): ");
        if (!scanner.hasNextLine()) {
            out.println();
            return false;
        }
        String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        out.println();
        return input.startsWith("y");
    }

    public void showGoodbye() {
        out.println("Thanks for playing LexiMatch.");
    }

    private String renderEvaluation(GuessEvaluation evaluation) {
        StringBuilder builder = new StringBuilder();
        for (LetterResult result : evaluation.results()) {
            builder.append('[')
                    .append(Character.toUpperCase(result.letter()))
                    .append(':')
                    .append(symbolFor(result.status()))
                    .append(']');
        }
        return builder.toString();
    }

    private char symbolFor(LetterStatus status) {
        return switch (status) {
            case CORRECT -> 'G';
            case PRESENT -> 'Y';
            case ABSENT -> 'X';
        };
    }
}
