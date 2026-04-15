package chess;

public record Position(int row, int col) {
    public static Position fromAlgebraic(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Square must be like e2.");
        }

        char file = Character.toLowerCase(notation.charAt(0));
        char rank = notation.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid square: " + notation);
        }

        int col = file - 'a';
        int row = 8 - Character.getNumericValue(rank);
        return new Position(row, col);
    }

    public String toAlgebraic() {
        return String.valueOf((char) ('a' + col)) + (8 - row);
    }

    public boolean isOnBoard() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
