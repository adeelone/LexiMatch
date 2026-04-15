package com.leximatch.controller;

import com.leximatch.model.DictionaryService;
import com.leximatch.model.GameDifficulty;
import com.leximatch.model.GameSession;
import com.leximatch.model.GuessEvaluation;
import com.leximatch.view.ConsoleView;

public final class GameController {
    private final DictionaryService dictionaryService;
    private final ConsoleView view;

    public GameController(DictionaryService dictionaryService, ConsoleView view) {
        this.dictionaryService = dictionaryService;
        this.view = view;
    }

    public void start() {
        view.showWelcome(dictionaryService.getDictionarySize(), dictionaryService.getAnswerBankSize());

        boolean keepPlaying = true;
        while (keepPlaying) {
            GameDifficulty difficulty = view.promptDifficulty();
            if (difficulty == null) {
                break;
            }
            GameSession session = new GameSession(difficulty, dictionaryService.randomAnswer());
            view.showDifficultyRules(difficulty);

            while (!session.isComplete()) {
                view.showBoard(session);
                String guess = view.promptGuess(session);
                if (guess == null || guess.equalsIgnoreCase("quit")) {
                    keepPlaying = false;
                    break;
                }
                GuessEvaluation evaluation = session.submitGuess(guess, dictionaryService);
                view.showEvaluation(evaluation);
            }

            if (!keepPlaying) {
                break;
            }
            view.showBoard(session);
            view.showGameResult(session);
            keepPlaying = view.promptReplay();
        }

        view.showGoodbye();
    }
}
