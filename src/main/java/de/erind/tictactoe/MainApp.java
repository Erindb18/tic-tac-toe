package de.erind.tictactoe;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main JavaFX application class for the Tic-Tac-Toe game.
 *
 * Responsibilities:
 * - Builds and manages the JavaFX user interface
 * - Connects UI interactions with game logic (GameState)
 * - Coordinates AI moves and game flow
 */
public class MainApp extends Application {

    /** Artificial delay (ms) before the AI makes a move */
    private static final int AI_DELAY_MS = 400;

    /** UI grid buttons representing the game board */
    private final Button[][] cells = new Button[GameState.SIZE][GameState.SIZE];

    /** Core game logic and state */
    private final GameState game = new GameState();

    /** Status text (current turn, win, draw) */
    private Label statusLabel;

    /** Scoreboard text */
    private Label scoreLabel;

    /* -------------------- Score tracking -------------------- */

    private int player1Wins = 0;
    private int player2Wins = 0;
    private int aiWins = 0;
    private int draws = 0;

    /* -------------------- UI controls -------------------- */

    private CheckBox vsAiCheck;
    private Label difficultyLabel;
    private ComboBox<String> difficultyBox;

    /* -------------------- Game mode state -------------------- */

    /** True if playing against AI, false for two-player mode */
    private boolean vsAiMode = true;

    /**
     * Controls which player starts the next round.
     * false -> Player 1 starts (X)
     * true  -> Second player starts (AI or Player 2)
     */
    private boolean secondPlayerStarts = false;

    /* -------------------- Internal symbols -------------------- */

    /** Symbol used by Player 1 for the current round */
    private String player1Symbol = "X";

    /** Symbol used by second player (AI or Player 2) */
    private String secondPlayerSymbol = "O";

    /**
     * JavaFX entry point.
     * Builds the UI, wires event handlers, and starts the first game.
     */
    @Override
    public void start(Stage stage) {
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        scoreLabel = new Label();
        scoreLabel.getStyleClass().add("score-label");

        vsAiCheck = new CheckBox("Play vs AI");
        vsAiCheck.setSelected(true);
        vsAiMode = true;

        difficultyLabel = new Label("Difficulty:");
        difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("Easy", "Hard");
        difficultyBox.setValue("Easy");

        // Show/hide difficulty depending on selected mode
        updateDifficultyVisibility();

        vsAiCheck.setOnAction(e -> {
            vsAiMode = vsAiCheck.isSelected();
            updateDifficultyVisibility();
            resetBoard(false);
            scoreLabel.setText(scoreText());
        });

        difficultyBox.setOnAction(e -> resetBoard(false));

        GridPane board = createBoard();

        Button newGameButton = new Button("New Game");
        newGameButton.getStyleClass().add("primary-button");
        newGameButton.setOnAction(e -> resetBoard(true));

        Button resetScoreButton = new Button("Reset Score");
        resetScoreButton.getStyleClass().add("primary-button");
        resetScoreButton.setOnAction(e -> {
            player1Wins = 0;
            player2Wins = 0;
            aiWins = 0;
            draws = 0;
            scoreLabel.setText(scoreText());
        });

        HBox bottomBar = new HBox(10, newGameButton, resetScoreButton);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar());
        root.setCenter(board);
        root.setBottom(bottomBar);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 640, 520);

        // Load external CSS styling
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Tic-Tac-Toe");
        stage.setScene(scene);
        stage.show();

        // Start first round
        resetBoard(false);
    }

    /**
     * Builds the top control bar (status, score, mode selection).
     */
    private HBox topBar() {
        HBox top = new HBox(
                14,
                statusLabel,
                scoreLabel,
                vsAiCheck,
                difficultyLabel,
                difficultyBox
        );
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(10));
        top.setSpacing(18);
        return top;
    }

    /**
     * Shows or hides the difficulty controls depending on the game mode.
     * setManaged(false) is used so hidden elements do not take layout space.
     */
    private void updateDifficultyVisibility() {
        difficultyLabel.setVisible(vsAiMode);
        difficultyLabel.setManaged(vsAiMode);

        difficultyBox.setVisible(vsAiMode);
        difficultyBox.setManaged(vsAiMode);
    }

    /**
     * Creates the clickable 3x3 game board.
     */
    private GridPane createBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(12);
        grid.setVgap(12);

        for (int r = 0; r < GameState.SIZE; r++) {
            for (int c = 0; c < GameState.SIZE; c++) {
                Button cell = new Button("");
                cell.setPrefSize(120, 120);
                cell.getStyleClass().add("board-button");

                final int row = r;
                final int col = c;
                cell.setOnAction(e -> handleClick(row, col));

                cells[r][c] = cell;
                grid.add(cell, c, r);
            }
        }

        return grid;
    }

    /**
     * Handles a user clicking on a board cell.
     */
    private void handleClick(int row, int col) {
        if (game.isGameOver()) return;

        // In AI mode, block input when it is the AI's turn
        if (vsAiMode && !isPlayer1Turn()) return;

        GameState.MoveResult result = game.play(row, col);
        refreshBoard();
        applyResult(result);

        if (vsAiMode && !game.isGameOver()) {
            maybeAiMove();
        }
    }

    /**
     * Determines whether it is Player 1's turn.
     */
    private boolean isPlayer1Turn() {
        return game.isXTurn()
                ? "X".equals(player1Symbol)
                : "O".equals(player1Symbol);
    }

    /**
     * Triggers the AI move after a short delay.
     * The board is temporarily disabled to prevent input.
     */
    private void maybeAiMove() {
        if (!vsAiMode || game.isGameOver() || isPlayer1Turn()) return;

        disableBoard();

        PauseTransition pause = new PauseTransition(Duration.millis(AI_DELAY_MS));
        pause.setOnFinished(event -> {
            TicTacToeAI.Difficulty diff =
                    "Hard".equals(difficultyBox.getValue())
                            ? TicTacToeAI.Difficulty.HARD
                            : TicTacToeAI.Difficulty.EASY;

            int[] move = TicTacToeAI.chooseMove(
                    game.getBoardCopy(),
                    secondPlayerSymbol,
                    player1Symbol,
                    diff
            );

            if (move != null) {
                GameState.MoveResult result = game.play(move[0], move[1]);
                refreshBoard();
                applyResult(result);
            }

            if (!game.isGameOver()) {
                enableBoard();
            }
        });

        pause.play();
    }

    /**
     * Applies the result of a move (continue, win, draw).
     */
    private void applyResult(GameState.MoveResult result) {
        switch (result.type) {
            case CONTINUE -> updateTurnText();
            case DRAW -> {
                draws++;
                statusLabel.setText("Draw");
                scoreLabel.setText(scoreText());
                disableBoard();
            }
            case WIN -> {
                awardWin(result.winner);
                statusLabel.setText(winnerText(result.winner));
                scoreLabel.setText(scoreText());
                highlightWinningLine(result.winLine);
                disableBoard();
            }
            default -> { }
        }
    }

    /**
     * Updates score counters based on the winning symbol.
     */
    private void awardWin(String winnerSymbol) {
        if (winnerSymbol.equals(player1Symbol)) {
            player1Wins++;
        } else if (vsAiMode) {
            aiWins++;
        } else {
            player2Wins++;
        }
    }

    /**
     * Resets the board for a new round.
     */
    private void resetBoard(boolean toggleStarter) {
        if (toggleStarter) {
            secondPlayerStarts = !secondPlayerStarts;
        }

        // Starter always plays X
        if (!secondPlayerStarts) {
            player1Symbol = "X";
            secondPlayerSymbol = "O";
        } else {
            player1Symbol = "O";
            secondPlayerSymbol = "X";
        }

        game.reset();

        for (int r = 0; r < GameState.SIZE; r++) {
            for (int c = 0; c < GameState.SIZE; c++) {
                Button cell = cells[r][c];
                cell.setText("");
                cell.setDisable(false);
                cell.getStyleClass().remove("win-cell");
            }
        }

        updateTurnText();
        scoreLabel.setText(scoreText());

        // Let AI start if applicable
        if (vsAiMode && secondPlayerStarts) {
            maybeAiMove();
        }
    }

    /**
     * Updates the status label to reflect whose turn it is.
     */
    private void updateTurnText() {
        if (isPlayer1Turn()) {
            statusLabel.setText("Player 1's move");
        } else {
            statusLabel.setText(vsAiMode ? "AI's move" : "Player 2's move");
        }
    }

    /**
     * Returns a user-friendly win message.
     */
    private String winnerText(String winnerSymbol) {
        if (winnerSymbol.equals(player1Symbol)) {
            return "Player 1 wins";
        }
        return vsAiMode ? "AI wins" : "Player 2 wins";
    }

    /**
     * Syncs UI board buttons with the current game state.
     */
    private void refreshBoard() {
        for (int r = 0; r < GameState.SIZE; r++) {
            for (int c = 0; c < GameState.SIZE; c++) {
                cells[r][c].setText(game.getCell(r, c));
            }
        }
    }

    /** Disables all board buttons. */
    private void disableBoard() {
        for (int r = 0; r < GameState.SIZE; r++) {
            for (int c = 0; c < GameState.SIZE; c++) {
                cells[r][c].setDisable(true);
            }
        }
    }

    /** Enables all board buttons. */
    private void enableBoard() {
        for (int r = 0; r < GameState.SIZE; r++) {
            for (int c = 0; c < GameState.SIZE; c++) {
                cells[r][c].setDisable(false);
            }
        }
    }

    /**
     * Highlights the winning line on the board.
     */
    private void highlightWinningLine(int[][] line) {
        if (line == null) return;
        for (int[] pos : line) {
            cells[pos[0]][pos[1]].getStyleClass().add("win-cell");
        }
    }

    /**
     * Builds the scoreboard text depending on the current mode.
     */
    private String scoreText() {
        return vsAiMode
                ? "Player 1: " + player1Wins + "   AI: " + aiWins + "   Draws: " + draws
                : "Player 1: " + player1Wins + "   Player 2: " + player2Wins + "   Draws: " + draws;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
