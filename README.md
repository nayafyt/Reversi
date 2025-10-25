# Reversi Game

A Java implementation of the classic Reversi (Othello) board game with AI opponent using minimax algorithm.

## Features

- Command line interface
- AI opponent with 3 difficulty levels:
  - Novice
  - Medium 
  - Expert
- Sophisticated AI evaluation including:
  - Corner control
  - Mobility analysis
  - Frontier discs strategy
  - Stable discs counting
  - Center control weighting
  - Wedge patterns detection
- Move validation and dynamic board updates

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Project Structure

```
src/main/java/com/aueb/
├── Board.java      # Game board and evaluation logic
├── Move.java       # Move representation
├── Player.java     # AI player implementation
└── Main.java       # Game entry point and UI
```

## Building the Project

```bash
mvn clean install
```

## Running the Game

```bash
mvn exec:java -Dexec.mainClass="com.aueb.Main"
```

## How to Play

1. Choose your color (B for Black or W for White)
2. Select difficulty level (1-3)
3. On your turn:
   - View available moves
   - Enter the number of your chosen move
   - Watch the board update

Game ends when:
- No valid moves remain
- Board is full
- All pieces are one color

## Technical Details

The AI uses:
- Minimax algorithm with varying depths based on difficulty
- Complex position evaluation including:
  - Corner ownership
  - Edge stability
  - Disc mobility
  - Center control
  - Frontier minimization

## Authors

Naya Fytali, Sara Mourelatou, Sofia Vergi

## License

This project is available under the MIT License.
