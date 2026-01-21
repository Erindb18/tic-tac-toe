package de.erind.tictactoe;

/**
 * Represents the core game logic and state of a Tic-Tac-Toe game.
 *
 * Responsibilities:
 * - Maintain the board state
 * - Track whose turn it is
 * - Detect wins and draws
 * - Validate moves
 */
public class GameState {

    /** Board size (3x3 for classic Tic-Tac-Toe) */
    public static final int SIZE = 3;

    /** Internal board representation */
    private final String[][] board = new String[SIZE][SIZE];

    /** True if it is X's turn, false if it is O's turn */
    private boolean xTurn = true;

    /** True once the game has ended (win or draw) */
    private boolean gameOver = false;

    /** Creates a new game with an empty board */
    public GameState() {
        reset();
    }

    public boolean isXTurn() {
        return xTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the symbol stored at a given board position.
     */
    public String getCell(int row, int col) {
        return board[row][col];
    }

    /**
     * Attempts to place the current player's symbol at (row, col).
     */
    public MoveResult play(int row, int col) {
        // Reject moves if the game is already over
        if (gameOver) {
            return MoveResult.invalid("Game is over.");
        }

        // Reject moves on already occupied cells
        if (!board[row][col].isEmpty()) {
            return MoveResult.invalid("Cell already used.");
        }

        // Place current player's symbol
        String symbol = xTurn ? "X" : "O";
        board[row][col] = symbol;

        // Check for a winning condition
        int[][] winLine = findWinningLine(symbol);
        if (winLine != null) {
            gameOver = true;
            return MoveResult.win(symbol, winLine);
        }

        // Check for draw (board full)
        if (isBoardFull()) {
            gameOver = true;
            return MoveResult.draw();
        }

        // Switch turns and continue game
        xTurn = !xTurn;
        return MoveResult.continueGame(xTurn ? "X" : "O");
    }

    /**
     * Resets the game to its initial state.
     * Clears the board and sets the turn to X.
     */
    public void reset() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = "";
            }
        }
        xTurn = true;
        gameOver = false;
    }

    /**
     * @return true if all board cells are filled
     */
    private boolean isBoardFull() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether the given symbol has a winning line.
     */
    private int[][] findWinningLine(String symbol) {
        // Check rows
        for (int r = 0; r < SIZE; r++) {
            if (symbol.equals(board[r][0])
                    && symbol.equals(board[r][1])
                    && symbol.equals(board[r][2])) {
                return new int[][] { {r, 0}, {r, 1}, {r, 2} };
            }
        }

        // Check columns
        for (int c = 0; c < SIZE; c++) {
            if (symbol.equals(board[0][c])
                    && symbol.equals(board[1][c])
                    && symbol.equals(board[2][c])) {
                return new int[][] { {0, c}, {1, c}, {2, c} };
            }
        }

        // Check diagonals
        if (symbol.equals(board[0][0])
                && symbol.equals(board[1][1])
                && symbol.equals(board[2][2])) {
            return new int[][] { {0, 0}, {1, 1}, {2, 2} };
        }

        if (symbol.equals(board[0][2])
                && symbol.equals(board[1][1])
                && symbol.equals(board[2][0])) {
            return new int[][] { {0, 2}, {1, 1}, {2, 0} };
        }

        return null;
    }

    /**
     * Creates a defensive copy of the board.
     * Used by the AI to analyze possible moves without mutating game state.
     */
    public String[][] getBoardCopy() {
        String[][] copy = new String[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                copy[r][c] = board[r][c];
            }
        }
        return copy;
    }

    /**
     * Immutable value object describing the result of a move.
     */
    public static final class MoveResult {

        /** Possible outcomes of a move */
        public enum Type {
            INVALID,   // Illegal move
            CONTINUE,  // Game continues
            WIN,       // A player has won
            DRAW       // Game ended in a draw
        }

        public final Type type;
        public final String message;

        /** Winner symbol ("X" or "O"), only set for WIN */
        public final String winner;

        /** Winning line coordinates, only set for WIN */
        public final int[][] winLine;

        private MoveResult(Type type, String message, String winner, int[][] winLine) {
            this.type = type;
            this.message = message;
            this.winner = winner;
            this.winLine = winLine;
        }

        /** Creates an invalid move result */
        public static MoveResult invalid(String msg) {
            return new MoveResult(Type.INVALID, msg, null, null);
        }

        /** Creates a continue-game result */
        public static MoveResult continueGame(String nextPlayer) {
            return new MoveResult(Type.CONTINUE, nextPlayer + " to move", null, null);
        }

        /** Creates a win result */
        public static MoveResult win(String winner, int[][] winLine) {
            return new MoveResult(Type.WIN, winner + " wins!", winner, winLine);
        }

        /** Creates a draw result */
        public static MoveResult draw() {
            return new MoveResult(Type.DRAW, "Draw!", null, null);
        }
    }
}
