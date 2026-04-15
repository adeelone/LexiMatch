package chess;

import java.util.List;

public final class ChessSmokeTest {
    private ChessSmokeTest() {
    }

    public static void main(String[] args) {
        testOpeningMoveCounts();
        testScholarMatePatternCheckDetection();
        testAiFindsLegalMove();
        System.out.println("All chess smoke tests passed.");
    }

    private static void testOpeningMoveCounts() {
        Board board = new Board();
        assertEquals(20, board.generateLegalMoves(Color.WHITE).size(), "White should have 20 legal opening moves.");
        assertEquals(20, board.generateLegalMoves(Color.BLACK).size(), "Black should have 20 legal opening moves.");
    }

    private static void testScholarMatePatternCheckDetection() {
        Board board = new Board();
        board = board.applyMove(parse(board, "e2e4"));
        board = board.applyMove(parse(board, "e7e5"));
        board = board.applyMove(parse(board, "d1h5"));
        board = board.applyMove(parse(board, "b8c6"));
        board = board.applyMove(parse(board, "f1c4"));
        board = board.applyMove(parse(board, "g8f6"));
        board = board.applyMove(parse(board, "h5f7"));

        assertTrue(board.isCheck(Color.BLACK), "Black king should be in check after Qxf7+.");
        assertTrue(board.gameResult() == GameResult.WHITE_WINS, "Position should be checkmate in the scholar's mate line.");
    }

    private static void testAiFindsLegalMove() {
        Board board = new Board();
        board = board.applyMove(parse(board, "e2e4"));
        MinimaxAI ai = new MinimaxAI(Color.BLACK, 3);
        MinimaxAI.SearchResult result = ai.chooseMove(board);
        List<Move> legalMoves = board.generateLegalMoves(Color.BLACK);

        assertTrue(result.move() != null, "AI should produce a move.");
        assertTrue(legalMoves.contains(result.move()), "AI move must be legal.");
    }

    private static Move parse(Board board, String notation) {
        return MoveParser.parse(notation, board.generateLegalMoves(board.turn()));
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " Expected " + expected + " but got " + actual + ".");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
