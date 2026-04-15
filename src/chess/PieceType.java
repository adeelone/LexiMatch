package chess;

public enum PieceType {
    KING(20_000, 'K'),
    QUEEN(900, 'Q'),
    ROOK(500, 'R'),
    BISHOP(330, 'B'),
    KNIGHT(320, 'N'),
    PAWN(100, 'P');

    private final int materialValue;
    private final char symbol;

    PieceType(int materialValue, char symbol) {
        this.materialValue = materialValue;
        this.symbol = symbol;
    }

    public int materialValue() {
        return materialValue;
    }

    public char symbol() {
        return symbol;
    }
}
