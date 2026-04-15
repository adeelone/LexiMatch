package com.leximatch;

import com.leximatch.controller.GameController;
import com.leximatch.model.DictionaryService;
import com.leximatch.view.ConsoleView;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        DictionaryService dictionaryService = new DictionaryService(
                "/allowed-guesses.txt",
                "/answer-bank.txt");
        ConsoleView view = new ConsoleView(System.in, System.out);
        GameController controller = new GameController(dictionaryService, view);
        controller.start();
    }
}
