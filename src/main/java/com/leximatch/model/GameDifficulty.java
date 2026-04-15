package com.leximatch.model;

public enum GameDifficulty {
    BEGINNER("Beginner", 8, false, true),
    STANDARD("Standard", 6, false, false),
    ADVANCED("Advanced", 6, true, false),
    EXPERT("Expert", 5, true, false);

    private final String label;
    private final int maxAttempts;
    private final boolean hardMode;
    private final boolean revealHint;

    GameDifficulty(String label, int maxAttempts, boolean hardMode, boolean revealHint) {
        this.label = label;
        this.maxAttempts = maxAttempts;
        this.hardMode = hardMode;
        this.revealHint = revealHint;
    }

    public String label() {
        return label;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public boolean hardMode() {
        return hardMode;
    }

    public boolean revealHint() {
        return revealHint;
    }
}
