package chess;

import java.util.Comparator;
import java.util.List;

public final class MinimaxAI {
    private final Color aiColor;
    private final int searchDepth;
    private long nodesEvaluated;

    public MinimaxAI(Color aiColor, int searchDepth) {
        this.aiColor = aiColor;
        this.searchDepth = searchDepth;
    }

    public SearchResult chooseMove(Board board) {
        long start = System.nanoTime();
        nodesEvaluated = 0;

        List<Move> legalMoves = board.generateLegalMoves(aiColor);
        if (legalMoves.isEmpty()) {
            return new SearchResult(null, 0, 0, 0);
        }

        legalMoves.sort(Comparator.comparingInt((Move move) -> moveOrderingScore(board, move)).reversed());

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = legalMoves.getFirst();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            Board next = board.applyMove(move);
            int score = minimax(next, searchDepth - 1, alpha, beta, false);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        return new SearchResult(bestMove, bestScore, nodesEvaluated, elapsedMs);
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing) {
        GameResult result = board.gameResult();
        if (depth == 0 || result != GameResult.ACTIVE) {
            nodesEvaluated++;
            return Evaluator.evaluate(board, aiColor);
        }

        Color sideToMove = maximizing ? aiColor : aiColor.opposite();
        List<Move> legalMoves = board.generateLegalMoves(sideToMove);
        legalMoves.sort(Comparator.comparingInt((Move move) -> moveOrderingScore(board, move)).reversed());

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                value = Math.max(value, minimax(board.applyMove(move), depth - 1, alpha, beta, false));
                alpha = Math.max(alpha, value);
                if (beta <= alpha) {
                    break;
                }
            }
            return value;
        }

        int value = Integer.MAX_VALUE;
        for (Move move : legalMoves) {
            value = Math.min(value, minimax(board.applyMove(move), depth - 1, alpha, beta, true));
            beta = Math.min(beta, value);
            if (beta <= alpha) {
                break;
            }
        }
        return value;
    }

    private int moveOrderingScore(Board board, Move move) {
        Piece attacker = board.getPiece(move.from());
        Piece target = board.getPiece(move.to());
        int score = 0;
        if (target != null) {
            score += (10 * target.type().materialValue()) - attacker.type().materialValue();
        }
        if (move.isPromotion()) {
            score += move.promotion().materialValue();
        }
        if (move.isCastle()) {
            score += 50;
        }
        return score;
    }

    public record SearchResult(Move move, int score, long nodes, long elapsedMs) {
    }
}
