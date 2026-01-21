package de.erind.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple AI engine for Tic-Tac-Toe.
 *
 * Supports two difficulty levels:
 * - EASY: chooses a random available move
 * - HARD: uses minimax for perfect play
 */
public final class TicTacToeAI {

    /** AI difficulty levels */
    public enum Difficulty { EASY, HARD }

    /** RNG used for EASY difficulty */
    private static final Random RNG = new Random();

    /** Utility class: prevent instantiation */
    private TicTacToeAI() {}

    /**
     * Chooses a move for the AI.
     */
    public static int[] chooseMove(String[][] board,
                                   String aiSymbol,
                                   String humanSymbol,
                                   Difficulty difficulty) {

        List<int[]> moves = availableMoves(board);
        if (moves.isEmpty()) return null;

        // EASY: pick a random free cell
        if (difficulty == Difficulty.EASY) {
            return moves.get(RNG.nextInt(moves.size()));
        }

        // HARD: minimax (optimal play)
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int[] move : moves) {
            // Try the move
            board[move[0]][move[1]] = aiSymbol;

            // Evaluate outcome assuming the human plays next
            int score = minimax(board, 0, false, aiSymbol, humanSymbol);

            // Undo the move (restore board)
            board[move[0]][move[1]] = "";

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Minimax algorithm for Tic-Tac-Toe.
     */
    private static int minimax(String[][] board,
                               int depth,
                               boolean maximizing,
                               String ai,
                               String human) {

        // Terminal state evaluation
        String winner = winner(board);
        if (winner != null) {
            if (winner.equals(ai)) return 10 - depth;     // prefer faster win
            if (winner.equals(human)) return depth - 10;  // prefer slower loss
        }

        // No moves left -> draw
        if (availableMoves(board).isEmpty()) return 0;

        if (maximizing) {
            // AI tries to maximize the score
            int best = Integer.MIN_VALUE;
            for (int[] m : availableMoves(board)) {
                board[m[0]][m[1]] = ai;
                best = Math.max(best, minimax(board, depth + 1, false, ai, human));
                board[m[0]][m[1]] = "";
            }
            return best;
        } else {
            // Human tries to minimize the score (worst-case for AI)
            int best = Integer.MAX_VALUE;
            for (int[] m : availableMoves(board)) {
                board[m[0]][m[1]] = human;
                best = Math.min(best, minimax(board, depth + 1, true, ai, human));
                board[m[0]][m[1]] = "";
            }
            return best;
        }
    }

    /**
     * Lists all currently available moves (empty cells).
     */
    private static List<int[]> availableMoves(String[][] board) {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                if (board[r][c].isEmpty()) {
                    moves.add(new int[]{r, c});
                }
            }
        }
        return moves;
    }

    /**
     * Detects a winner on the board.
     */
    private static String winner(String[][] b) {
        // Rows
        for (int r = 0; r < 3; r++) {
            if (!b[r][0].isEmpty() && b[r][0].equals(b[r][1]) && b[r][0].equals(b[r][2])) {
                return b[r][0];
            }
        }

        // Columns
        for (int c = 0; c < 3; c++) {
            if (!b[0][c].isEmpty() && b[0][c].equals(b[1][c]) && b[0][c].equals(b[2][c])) {
                return b[0][c];
            }
        }

        // Diagonals
        if (!b[0][0].isEmpty() && b[0][0].equals(b[1][1]) && b[0][0].equals(b[2][2])) return b[0][0];
        if (!b[0][2].isEmpty() && b[0][2].equals(b[1][1]) && b[0][2].equals(b[2][0])) return b[0][2];

        return null;
    }
}
