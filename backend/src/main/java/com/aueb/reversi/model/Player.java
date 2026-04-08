package com.aueb.reversi.model;

import java.util.List;
import java.util.Random;

public class Player {
    private final int maxDepth;
    private final Random random = new Random();

    public Player(int maxDepth, int playerLetter) {
        this.maxDepth = maxDepth;
    }

    public Move max(Board board, int depth) {
        if (board.isTerminal() || depth == this.maxDepth) {
            return new Move(board.getLastMove().getRow(), board.getLastMove().getCol(), board.evaluate());
        }

        List<Board> children = board.getChildren(Board.B);

        Move maxMove = new Move(Integer.MIN_VALUE);
        for (Board child : children) {
            Move move = min(child, depth + 1);

            if (hasNoPosition(move)) {
                move = retryMax(child, depth);
            }

            if (move.getValue() >= maxMove.getValue()) {
                if (move.getValue() == maxMove.getValue()) {
                    if (random.nextInt(2) == 0) {
                        updateMove(maxMove, child.getLastMove(), move.getValue());
                    }
                } else {
                    updateMove(maxMove, child.getLastMove(), move.getValue());
                }
            }
        }
        return maxMove;
    }

    public Move min(Board board, int depth) {
        if (board.isTerminal() || depth == this.maxDepth) {
            return new Move(board.getLastMove().getRow(), board.getLastMove().getCol(), board.evaluate());
        }

        List<Board> children = board.getChildren(Board.W);

        Move minMove = new Move(Integer.MAX_VALUE);
        for (Board child : children) {
            Move move = max(child, depth + 1);

            if (hasNoPosition(move)) {
                move = retryMin(child, depth);
            }

            if (move.getValue() <= minMove.getValue()) {
                if (move.getValue() == minMove.getValue()) {
                    if (random.nextInt(2) == 0) {
                        updateMove(minMove, child.getLastMove(), move.getValue());
                    }
                } else {
                    updateMove(minMove, child.getLastMove(), move.getValue());
                }
            }
        }
        return minMove;
    }

    private Move retryMax(Board child, int depth) {
        for (int d = depth - 1; d >= 0; d--) {
            Move move = max(child, d);
            if (!hasNoPosition(move)) return move;
            if (maxDepth - d >= maxDepth - 1) break;
        }
        return max(child, 1);
    }

    private Move retryMin(Board child, int depth) {
        for (int d = depth - 1; d >= 0; d--) {
            Move move = min(child, d);
            if (!hasNoPosition(move)) return move;
            if (maxDepth - d >= maxDepth - 1) break;
        }
        return min(child, 1);
    }

    private boolean hasNoPosition(Move move) {
        return move.getCol() < 0 && move.getRow() < 0;
    }

    private void updateMove(Move target, Move source, int value) {
        target.setRow(source.getRow());
        target.setCol(source.getCol());
        target.setValue(value);
    }
}
