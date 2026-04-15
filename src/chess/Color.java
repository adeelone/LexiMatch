package chess;

public enum Color {
    WHITE(1),
    BLACK(-1);

    private final int pawnDirection;

    Color(int pawnDirection) {
        this.pawnDirection = pawnDirection;
    }

    public int pawnDirection() {
        return pawnDirection;
    }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
