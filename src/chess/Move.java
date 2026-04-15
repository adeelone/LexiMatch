package chess;

import java.util.Objects;

public final class Move {
    private final Position from;
    private final Position to;
    private final PieceType promotion;
    private final boolean enPassant;
    private final boolean castleKingSide;
    private final boolean castleQueenSide;

    public Move(Position from, Position to) {
        this(from, to, null, false, false, false);
    }

    public Move(Position from, Position to, PieceType promotion) {
        this(from, to, promotion, false, false, false);
    }

    public static Move enPassant(Position from, Position to) {
        return new Move(from, to, null, true, false, false);
    }

    public static Move castleKingSide(Position from, Position to) {
        return new Move(from, to, null, false, true, false);
    }

    public static Move castleQueenSide(Position from, Position to) {
        return new Move(from, to, null, false, false, true);
    }

    private Move(
            Position from,
            Position to,
            PieceType promotion,
            boolean enPassant,
            boolean castleKingSide,
            boolean castleQueenSide
    ) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
        this.enPassant = enPassant;
        this.castleKingSide = castleKingSide;
        this.castleQueenSide = castleQueenSide;
    }

    public Position from() {
        return from;
    }

    public Position to() {
        return to;
    }

    public PieceType promotion() {
        return promotion;
    }

    public boolean isPromotion() {
        return promotion != null;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public boolean isCastleKingSide() {
        return castleKingSide;
    }

    public boolean isCastleQueenSide() {
        return castleQueenSide;
    }

    public boolean isCastle() {
        return castleKingSide || castleQueenSide;
    }

    public String toCoordinateNotation() {
        String suffix = promotion == null ? "" : String.valueOf(Character.toLowerCase(promotion.symbol()));
        return from.toAlgebraic() + to.toAlgebraic() + suffix;
    }

    @Override
    public String toString() {
        return toCoordinateNotation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Move move)) {
            return false;
        }
        return enPassant == move.enPassant
                && castleKingSide == move.castleKingSide
                && castleQueenSide == move.castleQueenSide
                && Objects.equals(from, move.from)
                && Objects.equals(to, move.to)
                && promotion == move.promotion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, promotion, enPassant, castleKingSide, castleQueenSide);
    }
}
