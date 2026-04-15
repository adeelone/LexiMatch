package chess;

public final class Evaluator {
    private Evaluator() {
    }

    public static int evaluate(Board board, Color perspective) {
        GameResult result = board.gameResult();
        if (result == GameResult.WHITE_WINS) {
            return perspective == Color.WHITE ? 100_000 : -100_000;
        }
        if (result == GameResult.BLACK_WINS) {
            return perspective == Color.BLACK ? 100_000 : -100_000;
        }
        if (result == GameResult.STALEMATE) {
            return 0;
        }

        int score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece == null) {
                    continue;
                }
                int material = piece.type().materialValue();
                int positional = positionalBonus(piece, row, col);
                int value = material + positional;
                score += piece.color() == perspective ? value : -value;
            }
        }
        return score;
    }

    private static int positionalBonus(Piece piece, int row, int col) {
        int perspectiveRow = piece.color() == Color.WHITE ? row : 7 - row;
        return switch (piece.type()) {
            case PAWN -> (6 - perspectiveRow) * 12 + centerWeight(col) * 4;
            case KNIGHT -> centerWeight(col) * 10 + centerWeight(7 - perspectiveRow) * 10;
            case BISHOP -> centerWeight(col) * 6 + centerWeight(7 - perspectiveRow) * 6;
            case ROOK -> (6 - perspectiveRow) * 2;
            case QUEEN -> centerWeight(col) * 2;
            case KING -> perspectiveRow >= 6 ? 25 : -centerWeight(col) * 5;
        };
    }

    private static int centerWeight(int coordinate) {
        return switch (coordinate) {
            case 3, 4 -> 3;
            case 2, 5 -> 2;
            case 1, 6 -> 1;
            default -> 0;
        };
    }
}
