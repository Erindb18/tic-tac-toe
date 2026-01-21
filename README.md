# Tic-Tac-Toe (JavaFX)

A fully playable Tic-Tac-Toe game built with Java and JavaFX.  
The project was created to practice clean application structure, basic AI, and graphical user interface development beyond console applications.

## Features

- Graphical user interface using JavaFX
- Two game modes:
    - Player vs Player
    - Player vs AI
- AI with multiple difficulty levels:
    - Easy (random moves)
    - Hard (minimax, optimal play)
- Alternating starting player for fair gameplay
- Win and draw detection
- Score tracking
- Styled user interface using JavaFX CSS

## Technologies Used

- Java
- JavaFX
- Gradle
- JavaFX CSS

## Project Structure

- `MainApp` – JavaFX application and UI logic
- `GameState` – Core game logic and rules
- `TicTacToeAI` – AI logic (random + minimax)

## How to Run

Clone the repository and run:

```bash
./gradlew run
