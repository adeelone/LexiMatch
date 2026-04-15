package com.leximatch.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GameSession {
    private final GameDifficulty difficulty;
    private final String targetWord;
    private final List<GuessEvaluation> history = new ArrayList<>();

    public GameSession(GameDifficulty difficulty, String targetWord) {
        this.difficulty = Objects.requireNonNull(difficulty);
        this.targetWord = DictionaryService.normalize(targetWord);
        if (this.targetWord.length() != 5) {
            throw new IllegalArgumentException("Target word must be five letters.");
        }
    }

    public GuessEvaluation submitGuess(String rawGuess, DictionaryService dictionaryService) {
        String guess = DictionaryService.normalize(rawGuess);

        if (guess.length() != 5) {
            return invalid(guess, "Enter a valid 5-letter word.");
        }
        if (!dictionaryService.isAllowedWord(guess)) {
            return invalid(guess, "That word is not in the LexiMatch dictionary.");
        }
        if (difficulty.hardMode()) {
            String hardModeError = validateHardMode(guess);
            if (hardModeError != null) {
                return invalid(guess, hardModeError);
            }
        }

        List<LetterResult> results = evaluateGuess(guess);
        boolean win = guess.equals(targetWord);
        GuessEvaluation evaluation = new GuessEvaluation(
                guess,
                results,
                true,
                win,
                win ? "Solved in " + (history.size() + 1) + " guess(es)." : "Guess recorded.");
        history.add(evaluation);
        return evaluation;
    }

    public boolean isComplete() {
        return isWon() || history.size() >= difficulty.maxAttempts();
    }

    public boolean isWon() {
        return !history.isEmpty() && history.get(history.size() - 1).win();
    }

    public int getAttemptsUsed() {
        return history.size();
    }

    public int getAttemptsRemaining() {
        return difficulty.maxAttempts() - history.size();
    }

    public GameDifficulty getDifficulty() {
        return difficulty;
    }

    public String getTargetWord() {
        return targetWord;
    }

    public List<GuessEvaluation> getHistory() {
        return List.copyOf(history);
    }

    public String getHint() {
        if (!difficulty.revealHint() || history.size() < 3) {
            return "";
        }
        return "Hint: the word starts with '" + Character.toUpperCase(targetWord.charAt(0)) + "'.";
    }

    private GuessEvaluation invalid(String guess, String message) {
        return new GuessEvaluation(guess, List.of(), false, false, message);
    }

    private String validateHardMode(String guess) {
        for (GuessEvaluation evaluation : history) {
            for (int index = 0; index < evaluation.results().size(); index++) {
                LetterResult result = evaluation.results().get(index);
                if (result.status() == LetterStatus.CORRECT && guess.charAt(index) != result.letter()) {
                    return "Hard mode: keep confirmed letters fixed in place.";
                }
            }

            for (LetterResult result : evaluation.results()) {
                if (result.status() == LetterStatus.PRESENT && guess.indexOf(result.letter()) < 0) {
                    return "Hard mode: include revealed letters in future guesses.";
                }
            }
        }
        return null;
    }

    private List<LetterResult> evaluateGuess(String guess) {
        List<LetterResult> results = new ArrayList<>();
        Map<Character, Integer> remaining = new HashMap<>();

        for (int index = 0; index < targetWord.length(); index++) {
            char target = targetWord.charAt(index);
            char letter = guess.charAt(index);
            if (letter != target) {
                remaining.merge(target, 1, Integer::sum);
            }
        }

        for (int index = 0; index < guess.length(); index++) {
            char letter = guess.charAt(index);
            char target = targetWord.charAt(index);
            if (letter == target) {
                results.add(new LetterResult(letter, LetterStatus.CORRECT));
            } else if (remaining.getOrDefault(letter, 0) > 0) {
                results.add(new LetterResult(letter, LetterStatus.PRESENT));
                remaining.put(letter, remaining.get(letter) - 1);
            } else {
                results.add(new LetterResult(letter, LetterStatus.ABSENT));
            }
        }

        return results;
    }
}
