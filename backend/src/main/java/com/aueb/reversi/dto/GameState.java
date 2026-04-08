package com.aueb.reversi.dto;

import java.util.List;

public class GameState {
    private int[][] board;
    private String currentPlayer;
    private String playerColor;
    private int blackCount;
    private int whiteCount;
    private boolean gameOver;
    private String winner;
    private List<int[]> availableMoves;
    private int[] lastMove;
    private String message;

    public int[][] getBoard() { return board; }
    public void setBoard(int[][] board) { this.board = board; }
    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
    public String getPlayerColor() { return playerColor; }
    public void setPlayerColor(String playerColor) { this.playerColor = playerColor; }
    public int getBlackCount() { return blackCount; }
    public void setBlackCount(int blackCount) { this.blackCount = blackCount; }
    public int getWhiteCount() { return whiteCount; }
    public void setWhiteCount(int whiteCount) { this.whiteCount = whiteCount; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public List<int[]> getAvailableMoves() { return availableMoves; }
    public void setAvailableMoves(List<int[]> availableMoves) { this.availableMoves = availableMoves; }
    public int[] getLastMove() { return lastMove; }
    public void setLastMove(int[] lastMove) { this.lastMove = lastMove; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
