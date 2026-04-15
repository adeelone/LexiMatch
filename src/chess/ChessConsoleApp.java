package chess;

import java.util.List;
import java.util.Scanner;

public final class ChessConsoleApp {
    private ChessConsoleApp() {
    }

    public static void main(String[] args) {
        System.out.println("Java Chess");
        System.out.println("Enter moves in coordinate notation like e2e4. Type 'quit' to exit.");

        try (Scanner scanner = new Scanner(System.in)) {
            Board board = new Board();
            Color humanColor = chooseColor(scanner);
            Color aiColor = humanColor.opposite();
            MinimaxAI ai = new MinimaxAI(aiColor, 3);

            while (board.gameResult() == GameResult.ACTIVE) {
                System.out.println();
                System.out.print(board.render());

                if (board.isCheck(board.turn())) {
                    System.out.println("Check on " + board.turn() + ".");
                }

                if (board.turn() == humanColor) {
                    List<Move> legalMoves = board.generateLegalMoves(humanColor);
                    Move move = promptForMove(scanner, legalMoves);
                    if (move == null) {
                        System.out.println("Game ended by player.");
                        return;
                    }
                    board = board.applyMove(move);
                } else {
                    System.out.println("AI thinking...");
                    MinimaxAI.SearchResult result = ai.chooseMove(board);
                    if (result.move() == null) {
                        break;
                    }
                    System.out.printf(
                            "AI plays %s | score=%d | nodes=%d | time=%d ms%n",
                            result.move().toCoordinateNotation(),
                            result.score(),
                            result.nodes(),
                            result.elapsedMs()
                    );
                    board = board.applyMove(result.move());
                }
            }

            System.out.println();
            System.out.print(board.render());
            announceResult(board.gameResult(), humanColor);
        }
    }

    private static Color chooseColor(Scanner scanner) {
        while (true) {
            System.out.print("Choose your side (white/black): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("white") || input.equals("w")) {
                return Color.WHITE;
            }
            if (input.equals("black") || input.equals("b")) {
                return Color.BLACK;
            }
            System.out.println("Please enter white or black.");
        }
    }

    private static Move promptForMove(Scanner scanner, List<Move> legalMoves) {
        while (true) {
            System.out.print("Your move: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("quit")) {
                return null;
            }
            try {
                return MoveParser.parse(input, legalMoves);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void announceResult(GameResult result, Color humanColor) {
        switch (result) {
            case WHITE_WINS -> System.out.println(humanColor == Color.WHITE ? "Checkmate. You win!" : "Checkmate. AI wins.");
            case BLACK_WINS -> System.out.println(humanColor == Color.BLACK ? "Checkmate. You win!" : "Checkmate. AI wins.");
            case STALEMATE -> System.out.println("Stalemate.");
            default -> System.out.println("Game over.");
        }
    }
}
