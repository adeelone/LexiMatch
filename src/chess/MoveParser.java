package chess;

import java.util.List;

public final class MoveParser {
    private MoveParser() {
    }

    public static Move parse(String input, List<Move> legalMoves) {
        String normalized = input == null ? "" : input.trim().toLowerCase();
        if (normalized.length() < 4 || normalized.length() > 5) {
            throw new IllegalArgumentException("Use moves like e2e4 or e7e8q.");
        }

        Position from = Position.fromAlgebraic(normalized.substring(0, 2));
        Position to = Position.fromAlgebraic(normalized.substring(2, 4));
        PieceType promotion = null;

        if (normalized.length() == 5) {
            promotion = switch (normalized.charAt(4)) {
                case 'q' -> PieceType.QUEEN;
                case 'r' -> PieceType.ROOK;
                case 'b' -> PieceType.BISHOP;
                case 'n' -> PieceType.KNIGHT;
                default -> throw new IllegalArgumentException("Promotion must be q, r, b, or n.");
            };
        }

        for (Move legalMove : legalMoves) {
            boolean sameSquares = legalMove.from().equals(from) && legalMove.to().equals(to);
            if (sameSquares && legalMove.promotion() == promotion) {
                return legalMove;
            }
        }

        throw new IllegalArgumentException("Illegal move: " + normalized);
    }
}
