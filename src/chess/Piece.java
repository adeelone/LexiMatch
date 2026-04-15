package chess;

public record Piece(PieceType type, Color color) {
    @Override
    public String toString() {
        char symbol = type.symbol();
        return String.valueOf(color == Color.WHITE ? symbol : Character.toLowerCase(symbol));
    }
}
