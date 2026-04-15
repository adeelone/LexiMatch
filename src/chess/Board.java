package chess;

import java.util.ArrayList;
import java.util.List;

public final class Board {
    private final Piece[][] squares;
    private final Color turn;
    private final boolean whiteKingSideCastle;
    private final boolean whiteQueenSideCastle;
    private final boolean blackKingSideCastle;
    private final boolean blackQueenSideCastle;
    private final Position enPassantTarget;
    private final int halfmoveClock;
    private final int fullmoveNumber;

    public Board() {
        this(initialSquares(), Color.WHITE, true, true, true, true, null, 0, 1);
    }

    private Board(
            Piece[][] squares,
            Color turn,
            boolean whiteKingSideCastle,
            boolean whiteQueenSideCastle,
            boolean blackKingSideCastle,
            boolean blackQueenSideCastle,
            Position enPassantTarget,
            int halfmoveClock,
            int fullmoveNumber
    ) {
        this.squares = squares;
        this.turn = turn;
        this.whiteKingSideCastle = whiteKingSideCastle;
        this.whiteQueenSideCastle = whiteQueenSideCastle;
        this.blackKingSideCastle = blackKingSideCastle;
        this.blackQueenSideCastle = blackQueenSideCastle;
        this.enPassantTarget = enPassantTarget;
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
    }

    public Color turn() {
        return turn;
    }

    public Piece getPiece(Position position) {
        return squares[position.row()][position.col()];
    }

    public boolean isCheck(Color color) {
        Position king = findKing(color);
        return king != null && isSquareAttacked(king, color.opposite());
    }

    public GameResult gameResult() {
        List<Move> moves = generateLegalMoves(turn);
        if (!moves.isEmpty()) {
            return GameResult.ACTIVE;
        }
        if (isCheck(turn)) {
            return turn == Color.WHITE ? GameResult.BLACK_WINS : GameResult.WHITE_WINS;
        }
        return GameResult.STALEMATE;
    }

    public List<Move> generateLegalMoves(Color color) {
        List<Move> pseudo = generatePseudoLegalMoves(color);
        List<Move> legal = new ArrayList<>();
        for (Move move : pseudo) {
            Board next = applyMove(move);
            if (!next.isCheck(color)) {
                legal.add(move);
            }
        }
        return legal;
    }

    public Board applyMove(Move move) {
        Piece[][] nextSquares = copySquares();
        Piece moving = nextSquares[move.from().row()][move.from().col()];
        Piece captured = nextSquares[move.to().row()][move.to().col()];

        nextSquares[move.from().row()][move.from().col()] = null;

        boolean nextWhiteKingSideCastle = whiteKingSideCastle;
        boolean nextWhiteQueenSideCastle = whiteQueenSideCastle;
        boolean nextBlackKingSideCastle = blackKingSideCastle;
        boolean nextBlackQueenSideCastle = blackQueenSideCastle;
        Position nextEnPassant = null;
        int nextHalfmove = halfmoveClock + 1;
        int nextFullmove = turn == Color.BLACK ? fullmoveNumber + 1 : fullmoveNumber;

        if (moving.type() == PieceType.PAWN || captured != null) {
            nextHalfmove = 0;
        }

        if (move.isEnPassant()) {
            int capturedRow = move.to().row() - moving.color().pawnDirection();
            captured = nextSquares[capturedRow][move.to().col()];
            nextSquares[capturedRow][move.to().col()] = null;
            nextHalfmove = 0;
        }

        Piece placed = move.isPromotion() ? new Piece(move.promotion(), moving.color()) : moving;
        nextSquares[move.to().row()][move.to().col()] = placed;

        if (move.isCastleKingSide()) {
            if (moving.color() == Color.WHITE) {
                nextSquares[7][5] = nextSquares[7][7];
                nextSquares[7][7] = null;
            } else {
                nextSquares[0][5] = nextSquares[0][7];
                nextSquares[0][7] = null;
            }
        } else if (move.isCastleQueenSide()) {
            if (moving.color() == Color.WHITE) {
                nextSquares[7][3] = nextSquares[7][0];
                nextSquares[7][0] = null;
            } else {
                nextSquares[0][3] = nextSquares[0][0];
                nextSquares[0][0] = null;
            }
        }

        if (moving.type() == PieceType.KING) {
            if (moving.color() == Color.WHITE) {
                nextWhiteKingSideCastle = false;
                nextWhiteQueenSideCastle = false;
            } else {
                nextBlackKingSideCastle = false;
                nextBlackQueenSideCastle = false;
            }
        }

        if (moving.type() == PieceType.ROOK) {
            if (move.from().row() == 7 && move.from().col() == 0) {
                nextWhiteQueenSideCastle = false;
            } else if (move.from().row() == 7 && move.from().col() == 7) {
                nextWhiteKingSideCastle = false;
            } else if (move.from().row() == 0 && move.from().col() == 0) {
                nextBlackQueenSideCastle = false;
            } else if (move.from().row() == 0 && move.from().col() == 7) {
                nextBlackKingSideCastle = false;
            }
        }

        if (captured != null && captured.type() == PieceType.ROOK) {
            if (move.to().row() == 7 && move.to().col() == 0) {
                nextWhiteQueenSideCastle = false;
            } else if (move.to().row() == 7 && move.to().col() == 7) {
                nextWhiteKingSideCastle = false;
            } else if (move.to().row() == 0 && move.to().col() == 0) {
                nextBlackQueenSideCastle = false;
            } else if (move.to().row() == 0 && move.to().col() == 7) {
                nextBlackKingSideCastle = false;
            }
        }

        if (moving.type() == PieceType.PAWN && Math.abs(move.from().row() - move.to().row()) == 2) {
            nextEnPassant = new Position((move.from().row() + move.to().row()) / 2, move.from().col());
        }

        return new Board(
                nextSquares,
                turn.opposite(),
                nextWhiteKingSideCastle,
                nextWhiteQueenSideCastle,
                nextBlackKingSideCastle,
                nextBlackQueenSideCastle,
                nextEnPassant,
                nextHalfmove,
                nextFullmove
        );
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            builder.append(8 - row).append(" ");
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                builder.append(piece == null ? "." : piece).append(" ");
            }
            builder.append(System.lineSeparator());
        }
        builder.append("  a b c d e f g h").append(System.lineSeparator());
        builder.append("Turn: ").append(turn).append(System.lineSeparator());
        return builder.toString();
    }

    private List<Move> generatePseudoLegalMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.color() == color) {
                    Position from = new Position(row, col);
                    switch (piece.type()) {
                        case PAWN -> addPawnMoves(from, piece, moves);
                        case KNIGHT -> addKnightMoves(from, piece, moves);
                        case BISHOP -> addSlidingMoves(from, piece, moves, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
                        case ROOK -> addSlidingMoves(from, piece, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
                        case QUEEN -> addSlidingMoves(from, piece, moves, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}});
                        case KING -> addKingMoves(from, piece, moves);
                    }
                }
            }
        }
        return moves;
    }

    private void addPawnMoves(Position from, Piece piece, List<Move> moves) {
        int direction = piece.color().pawnDirection();
        int startRow = piece.color() == Color.WHITE ? 6 : 1;
        int promotionRow = piece.color() == Color.WHITE ? 0 : 7;

        Position oneForward = new Position(from.row() - direction, from.col());
        if (oneForward.isOnBoard() && getPiece(oneForward) == null) {
            addPawnAdvanceOrPromotion(from, oneForward, promotionRow, moves);

            Position twoForward = new Position(from.row() - (2 * direction), from.col());
            if (from.row() == startRow && getPiece(twoForward) == null) {
                moves.add(new Move(from, twoForward));
            }
        }

        for (int dc : new int[]{-1, 1}) {
            Position target = new Position(from.row() - direction, from.col() + dc);
            if (!target.isOnBoard()) {
                continue;
            }

            Piece occupant = getPiece(target);
            if (occupant != null && occupant.color() != piece.color()) {
                addPawnAdvanceOrPromotion(from, target, promotionRow, moves);
            } else if (enPassantTarget != null && enPassantTarget.equals(target)) {
                moves.add(Move.enPassant(from, target));
            }
        }
    }

    private void addPawnAdvanceOrPromotion(Position from, Position to, int promotionRow, List<Move> moves) {
        if (to.row() == promotionRow) {
            moves.add(new Move(from, to, PieceType.QUEEN));
            moves.add(new Move(from, to, PieceType.ROOK));
            moves.add(new Move(from, to, PieceType.BISHOP));
            moves.add(new Move(from, to, PieceType.KNIGHT));
        } else {
            moves.add(new Move(from, to));
        }
    }

    private void addKnightMoves(Position from, Piece piece, List<Move> moves) {
        int[][] jumps = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        for (int[] jump : jumps) {
            Position to = new Position(from.row() + jump[0], from.col() + jump[1]);
            if (to.isOnBoard() && isEmptyOrEnemy(to, piece.color())) {
                moves.add(new Move(from, to));
            }
        }
    }

    private void addSlidingMoves(Position from, Piece piece, List<Move> moves, int[][] directions) {
        for (int[] direction : directions) {
            int row = from.row() + direction[0];
            int col = from.col() + direction[1];
            while (row >= 0 && row < 8 && col >= 0 && col < 8) {
                Position to = new Position(row, col);
                if (getPiece(to) == null) {
                    moves.add(new Move(from, to));
                } else {
                    if (getPiece(to).color() != piece.color()) {
                        moves.add(new Move(from, to));
                    }
                    break;
                }
                row += direction[0];
                col += direction[1];
            }
        }
    }

    private void addKingMoves(Position from, Piece piece, List<Move> moves) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                Position to = new Position(from.row() + dr, from.col() + dc);
                if (to.isOnBoard() && isEmptyOrEnemy(to, piece.color())) {
                    moves.add(new Move(from, to));
                }
            }
        }

        if (piece.color() == Color.WHITE && from.row() == 7 && from.col() == 4 && !isCheck(Color.WHITE)) {
            if (whiteKingSideCastle
                    && getPiece(new Position(7, 5)) == null
                    && getPiece(new Position(7, 6)) == null
                    && !isSquareAttacked(new Position(7, 5), Color.BLACK)
                    && !isSquareAttacked(new Position(7, 6), Color.BLACK)) {
                moves.add(Move.castleKingSide(from, new Position(7, 6)));
            }
            if (whiteQueenSideCastle
                    && getPiece(new Position(7, 1)) == null
                    && getPiece(new Position(7, 2)) == null
                    && getPiece(new Position(7, 3)) == null
                    && !isSquareAttacked(new Position(7, 3), Color.BLACK)
                    && !isSquareAttacked(new Position(7, 2), Color.BLACK)) {
                moves.add(Move.castleQueenSide(from, new Position(7, 2)));
            }
        } else if (piece.color() == Color.BLACK && from.row() == 0 && from.col() == 4 && !isCheck(Color.BLACK)) {
            if (blackKingSideCastle
                    && getPiece(new Position(0, 5)) == null
                    && getPiece(new Position(0, 6)) == null
                    && !isSquareAttacked(new Position(0, 5), Color.WHITE)
                    && !isSquareAttacked(new Position(0, 6), Color.WHITE)) {
                moves.add(Move.castleKingSide(from, new Position(0, 6)));
            }
            if (blackQueenSideCastle
                    && getPiece(new Position(0, 1)) == null
                    && getPiece(new Position(0, 2)) == null
                    && getPiece(new Position(0, 3)) == null
                    && !isSquareAttacked(new Position(0, 3), Color.WHITE)
                    && !isSquareAttacked(new Position(0, 2), Color.WHITE)) {
                moves.add(Move.castleQueenSide(from, new Position(0, 2)));
            }
        }
    }

    public boolean isSquareAttacked(Position square, Color attacker) {
        int pawnRow = square.row() + attacker.pawnDirection();
        for (int dc : new int[]{-1, 1}) {
            Position pos = new Position(pawnRow, square.col() + dc);
            if (pos.isOnBoard()) {
                Piece piece = getPiece(pos);
                if (piece != null && piece.color() == attacker && piece.type() == PieceType.PAWN) {
                    return true;
                }
            }
        }

        int[][] knightJumps = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        for (int[] jump : knightJumps) {
            Position pos = new Position(square.row() + jump[0], square.col() + jump[1]);
            if (pos.isOnBoard()) {
                Piece piece = getPiece(pos);
                if (piece != null && piece.color() == attacker && piece.type() == PieceType.KNIGHT) {
                    return true;
                }
            }
        }

        if (isAttackedBySliding(square, attacker, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}, PieceType.ROOK, PieceType.QUEEN)) {
            return true;
        }
        if (isAttackedBySliding(square, attacker, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}, PieceType.BISHOP, PieceType.QUEEN)) {
            return true;
        }

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                Position pos = new Position(square.row() + dr, square.col() + dc);
                if (pos.isOnBoard()) {
                    Piece piece = getPiece(pos);
                    if (piece != null && piece.color() == attacker && piece.type() == PieceType.KING) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isAttackedBySliding(Position square, Color attacker, int[][] directions, PieceType primary, PieceType secondary) {
        for (int[] direction : directions) {
            int row = square.row() + direction[0];
            int col = square.col() + direction[1];
            while (row >= 0 && row < 8 && col >= 0 && col < 8) {
                Piece piece = squares[row][col];
                if (piece != null) {
                    if (piece.color() == attacker && (piece.type() == primary || piece.type() == secondary)) {
                        return true;
                    }
                    break;
                }
                row += direction[0];
                col += direction[1];
            }
        }
        return false;
    }

    private boolean isEmptyOrEnemy(Position position, Color color) {
        Piece occupant = getPiece(position);
        return occupant == null || occupant.color() != color;
    }

    private Position findKing(Color color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.color() == color && piece.type() == PieceType.KING) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    private Piece[][] copySquares() {
        Piece[][] copy = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            System.arraycopy(squares[row], 0, copy[row], 0, 8);
        }
        return copy;
    }

    private static Piece[][] initialSquares() {
        Piece[][] squares = new Piece[8][8];
        PieceType[] backRank = {
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
        };

        for (int col = 0; col < 8; col++) {
            squares[0][col] = new Piece(backRank[col], Color.BLACK);
            squares[1][col] = new Piece(PieceType.PAWN, Color.BLACK);
            squares[6][col] = new Piece(PieceType.PAWN, Color.WHITE);
            squares[7][col] = new Piece(backRank[col], Color.WHITE);
        }

        return squares;
    }
}
