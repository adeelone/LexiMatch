package com.leximatch.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {
    private static final DictionaryService DICTIONARY =
            new DictionaryService("/allowed-guesses.txt", "/answer-bank.txt");

    @Test
    void marksCorrectAndPresentLettersAccurately() {
        GameSession session = new GameSession(GameDifficulty.STANDARD, "cabin");

        GuessEvaluation evaluation = session.submitGuess("panic", DICTIONARY);

        assertTrue(evaluation.valid());
        assertEquals(List.of(
                LetterStatus.ABSENT,
                LetterStatus.CORRECT,
                LetterStatus.PRESENT,
                LetterStatus.CORRECT,
                LetterStatus.PRESENT
        ), evaluation.results().stream().map(LetterResult::status).toList());
    }

    @Test
    void rejectsUnknownWords() {
        GameSession session = new GameSession(GameDifficulty.STANDARD, "cabin");

        GuessEvaluation evaluation = session.submitGuess("zzzzz", DICTIONARY);

        assertFalse(evaluation.valid());
        assertTrue(evaluation.message().contains("not in the LexiMatch dictionary"));
    }

    @Test
    void enforcesHardModePlacementRules() {
        GameSession session = new GameSession(GameDifficulty.ADVANCED, "cabin");

        GuessEvaluation first = session.submitGuess("candy", DICTIONARY);
        GuessEvaluation second = session.submitGuess("banjo", DICTIONARY);

        assertTrue(first.valid());
        assertFalse(second.valid());
        assertTrue(second.message().contains("keep confirmed letters fixed"));
    }

    @Test
    void revealsHintOnlyForBeginnerAfterThreeValidGuesses() {
        GameSession session = new GameSession(GameDifficulty.BEGINNER, "cabin");

        session.submitGuess("candy", DICTIONARY);
        session.submitGuess("caper", DICTIONARY);
        session.submitGuess("cable", DICTIONARY);

        assertTrue(session.getHint().contains("starts with 'C'"));
    }

    @Test
    void endsGameWhenSolved() {
        GameSession session = new GameSession(GameDifficulty.EXPERT, "cabin");

        GuessEvaluation evaluation = session.submitGuess("cabin", DICTIONARY);

        assertTrue(evaluation.win());
        assertTrue(session.isComplete());
        assertTrue(session.isWon());
    }
}
