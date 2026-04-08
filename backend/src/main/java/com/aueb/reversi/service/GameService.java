package com.aueb.reversi.service;

import com.aueb.reversi.dto.GameState;
import com.aueb.reversi.model.Board;
import com.aueb.reversi.model.Move;
import com.aueb.reversi.model.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private Board board;
    private Player computer;
    private int playerLetter;
    private int computerLetter;
    private int skipPlayer;
    private int skipComputer;
    private boolean gameOver;
    private String message;

    public GameService() {
        newGame("B", 1);
    }

    public GameState newGame(String playerColor, int level) {
        if (level < 1 || level > 3) level = 1;

        if ("W".equals(playerColor)) {
            playerLetter = Board.W;
            computerLetter = Board.B;
        } else {
            playerLetter = Board.B;
            computerLetter = Board.W;
        }

        board = new Board();
        computer = new Player(level + 1, computerLetter);
        skipPlayer = 0;
        skipComputer = 0;
        gameOver = false;
        message = null;

        // If computer is Black, it goes first (Black always starts)
        if (computerLetter == Board.B) {
            computerMove();
        }

        return buildState();
    }

    public GameState playerMove(int row, int col) {
        if (gameOver) {
            message = "Game is already over.";
            return buildState();
        }

        ArrayList<Move> available = board.getavailableMoves(playerLetter);
        boolean valid = false;
        for (Move m : available) {
            if (m.getRow() == row && m.getCol() == col) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            message = "Invalid move.";
            return buildState();
        }

        skipPlayer = 0;
        board.makeMove(row, col, playerLetter);
        board.switcher(new Move(row, col, playerLetter));
        message = null;

        if (checkGameOver()) return buildState();

        // Computer responds
        computerMove();

        if (checkGameOver()) return buildState();

        // Check if player has moves, if not, skip and let computer go again
        while (!gameOver && board.getavailableMoves(playerLetter).isEmpty()) {
            skipPlayer++;
            if (skipPlayer >= 1 && skipComputer >= 1) {
                gameOver = true;
                break;
            }
            message = "No moves available for you. Computer plays again.";
            computerMove();
            if (checkGameOver()) break;
        }

        return buildState();
    }

    public GameState getState() {
        return buildState();
    }

    private void computerMove() {
        ArrayList<Move> computerMoves = board.getavailableMoves(computerLetter);

        if (computerMoves.isEmpty()) {
            skipComputer++;
            if (skipPlayer >= 1 && skipComputer >= 1) {
                gameOver = true;
            }
            message = "Computer has no moves and skips.";
            return;
        }

        skipComputer = 0;
        Move aiMove;
        if (computerLetter == Board.B) {
            aiMove = computer.max(new Board(board), 0);
        } else {
            aiMove = computer.min(new Board(board), 0);
        }

        if (aiMove.getRow() < 0 || aiMove.getCol() < 0) {
            skipComputer++;
            if (skipPlayer >= 1 && skipComputer >= 1) gameOver = true;
            message = "Computer skips.";
            return;
        }

        board.makeMove(aiMove.getRow(), aiMove.getCol(), computerLetter);
        board.switcher(new Move(aiMove.getRow(), aiMove.getCol(), computerLetter));
    }

    private boolean checkGameOver() {
        if (board.isTerminal()) {
            gameOver = true;
            return true;
        }
        return false;
    }

    private GameState buildState() {
        GameState state = new GameState();
        state.setBoard(board.getGameBoard());
        state.setPlayerColor(playerLetter == Board.B ? "B" : "W");

        int bCount = 0, wCount = 0;
        int[][] gb = board.getGameBoard();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                if (gb[r][c] == Board.B) bCount++;
                else if (gb[r][c] == Board.W) wCount++;
            }
        state.setBlackCount(bCount);
        state.setWhiteCount(wCount);
        state.setGameOver(gameOver);

        if (gameOver) {
            if (bCount > wCount) state.setWinner("B");
            else if (wCount > bCount) state.setWinner("W");
            else state.setWinner("DRAW");
        }

        // Available moves for the player
        if (!gameOver) {
            ArrayList<Move> moves = board.getavailableMoves(playerLetter);
            List<int[]> movePairs = new ArrayList<>();
            for (Move m : moves) movePairs.add(new int[]{m.getRow(), m.getCol()});
            state.setAvailableMoves(movePairs);
            state.setCurrentPlayer("PLAYER");
        } else {
            state.setAvailableMoves(new ArrayList<>());
        }

        Move last = board.getLastMove();
        if (last != null && last.getRow() >= 0) {
            state.setLastMove(new int[]{last.getRow(), last.getCol()});
        }

        state.setMessage(message);
        message = null;

        return state;
    }
}
