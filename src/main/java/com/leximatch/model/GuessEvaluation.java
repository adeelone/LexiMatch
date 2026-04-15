package com.leximatch.model;

import java.util.List;

public record GuessEvaluation(
        String guess,
        List<LetterResult> results,
        boolean valid,
        boolean win,
        String message) {
}
