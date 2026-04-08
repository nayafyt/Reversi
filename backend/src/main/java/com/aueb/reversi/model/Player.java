package com.aueb.reversi.model;

import java.util.ArrayList;
import java.util.Random;

public class Player {
    private int maxDepth;
    private int playerLetter;

    public Player() {}

    public Player(int maxDepth, int playerLetter) {
        this.maxDepth = maxDepth;
        this.playerLetter = playerLetter;
    }

    public Move max(Board board, int depth) {
        Random r = new Random();

        if (board.isTerminal() || depth == this.maxDepth) {
            return new Move(board.getLastMove().getRow(), board.getLastMove().getCol(), board.evaluate());
        }

        ArrayList<Board> children = board.getChildren(Board.B);

        Move maxMove = new Move(Integer.MIN_VALUE);
        for (Board child : children) {
            Move move = min(child, depth + 1);

            if (maxDepth == 2 && (move.getCol() < 0 && move.getRow() < 0)) {
                move = max(child, 1);
            } else if (maxDepth == 3 && (move.getCol() < 0 && move.getRow() < 0)) {
                move = max(child, depth - 1);
                if (move.getCol() < 0 && move.getRow() < 0) {
                    move = max(child, depth - 2);
                }
            } else if (maxDepth == 4 && (move.getCol() < 0 && move.getRow() < 0)) {
                move = max(child, depth - 1);
                if (move.getCol() < 0 && move.getRow() < 0) {
                    move = max(child, depth - 2);
                    if (move.getCol() < 0 && move.getRow() < 0) {
                        move = max(child, depth - 3);
                    }
                }
            }

            if (move.getValue() >= maxMove.getValue()) {
                if (move.getValue() == maxMove.getValue()) {
                    if (r.nextInt(2) == 0) {
                        maxMove.setRow(child.getLastMove().getRow());
                        maxMove.setCol(child.getLastMove().getCol());
                        maxMove.setValue(move.getValue());
                    }
                } else {
                    maxMove.setRow(child.getLastMove().getRow());
                    maxMove.setCol(child.getLastMove().getCol());
                    maxMove.setValue(move.getValue());
                }
            }
        }
        return maxMove;
    }

    public Move min(Board board, int depth) {
        Random r = new Random();

        if (board.isTerminal() || depth == this.maxDepth) {
            return new Move(board.getLastMove().getRow(), board.getLastMove().getCol(), board.evaluate());
        }

        ArrayList<Board> children = board.getChildren(Board.W);

        Move minMove = new Move(Integer.MAX_VALUE);
        for (Board child : children) {
            Move move = max(child, depth + 1);

            if (maxDepth == 2 && (move.getCol() < 0 && move.getRow() < 0)) {
                move = min(child, 1);
            } else if (maxDepth == 3 && (move.getCol() < 0 && move.getRow() < 0)) {
                move = min(child, depth - 1);
                if (move.getCol() < 0 && move.getRow() < 0) {
                    move = min(child, depth - 2);
                }
            } else if (maxDepth == 4 && (move.getCol() < 0 && move.getRow() < 0)) {
                move = min(child, depth - 1);
                if (move.getCol() < 0 && move.getRow() < 0) {
                    move = min(child, depth - 2);
                    if (move.getCol() < 0 && move.getRow() < 0) {
                        move = min(child, depth - 3);
                    }
                }
            }

            if (move.getValue() <= minMove.getValue()) {
                if (move.getValue() == minMove.getValue()) {
                    if (r.nextInt(2) == 0) {
                        minMove.setRow(child.getLastMove().getRow());
                        minMove.setCol(child.getLastMove().getCol());
                        minMove.setValue(move.getValue());
                    }
                } else {
                    minMove.setRow(child.getLastMove().getRow());
                    minMove.setCol(child.getLastMove().getCol());
                    minMove.setValue(move.getValue());
                }
            }
        }
        return minMove;
    }
}
