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

        List<Move> available = board.getAvailableMoves(playerLetter);
        boolean valid = available.stream()
                .anyMatch(m -> m.getRow() == row && m.getCol() == col);

        if (!valid) {
            message = "Invalid move.";
            return buildState();
        }

        skipPlayer = 0;
        board.makeMove(row, col, playerLetter);
        board.flipDiscs(new Move(row, col, playerLetter));
        message = null;

        if (checkGameOver()) return buildState();

        computerMove();

        if (checkGameOver()) return buildState();

        while (!gameOver && board.getAvailableMoves(playerLetter).isEmpty()) {
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
        if (board.getAvailableMoves(computerLetter).isEmpty()) {
            skipComputer++;
            if (skipPlayer >= 1 && skipComputer >= 1) {
                gameOver = true;
            }
            message = "Computer has no moves and skips.";
            return;
        }

        skipComputer = 0;
        Move aiMove = (computerLetter == Board.B)
                ? computer.max(new Board(board), 0)
                : computer.min(new Board(board), 0);

        if (aiMove.getRow() < 0 || aiMove.getCol() < 0) {
            skipComputer++;
            if (skipPlayer >= 1 && skipComputer >= 1) gameOver = true;
            message = "Computer skips.";
            return;
        }

        board.makeMove(aiMove.getRow(), aiMove.getCol(), computerLetter);
        board.flipDiscs(new Move(aiMove.getRow(), aiMove.getCol(), computerLetter));
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

        int bCount = 0;
        int wCount = 0;
        int[][] gb = board.getGameBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (gb[r][c] == Board.B) bCount++;
                else if (gb[r][c] == Board.W) wCount++;
            }
        }
        state.setBlackCount(bCount);
        state.setWhiteCount(wCount);
        state.setGameOver(gameOver);

        if (gameOver) {
            if (bCount > wCount) state.setWinner("B");
            else if (wCount > bCount) state.setWinner("W");
            else state.setWinner("DRAW");
        }

        if (!gameOver) {
            List<Move> moves = board.getAvailableMoves(playerLetter);
            List<int[]> movePairs = new ArrayList<>();
            for (Move m : moves) movePairs.add(new int[]{m.getRow(), m.getCol()});
            state.setAvailableMoves(movePairs);
            state.setCurrentPlayer("PLAYER");
        } else {
            state.setAvailableMoves(List.of());
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
