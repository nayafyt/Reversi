package com.aueb.reversi.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int W = 1;
    public static final int B = -1;
    public static final int EMPTY = 0;
    private static final int SIZE = 8;

    private int[][] gameBoard;
    private int lastPlayer;
    private Move lastMove;

    public Board() {
        this.lastMove = new Move();
        this.lastPlayer = W;
        this.gameBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                this.gameBoard[i][j] = EMPTY;
        this.gameBoard[3][3] = W;
        this.gameBoard[4][4] = W;
        this.gameBoard[3][4] = B;
        this.gameBoard[4][3] = B;
    }

    public Board(Board board) {
        this.lastMove = new Move(board.lastMove.getRow(), board.lastMove.getCol(), board.lastMove.getValue());
        this.lastPlayer = board.lastPlayer;
        this.gameBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                this.gameBoard[i][j] = board.gameBoard[i][j];
    }

    public void makeMove(int row, int col, int letter) {
        this.gameBoard[row][col] = letter;
        this.lastMove = new Move(row, col);
        this.lastPlayer = letter;
    }

    public List<Board> getChildren(int letter) {
        List<Board> children = new ArrayList<>();
        List<Move> moves = getAvailableMoves(letter);
        for (Move mv : moves) {
            Board child = new Board(this);
            child.makeMove(mv.getRow(), mv.getCol(), letter);
            child.flipDiscs(new Move(mv.getRow(), mv.getCol(), letter));
            children.add(child);
        }
        return children;
    }

    public boolean isTerminal() {
        int bPieces = 0;
        int wPieces = 0;
        boolean hasEmpty = false;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (this.gameBoard[row][col] == B) bPieces++;
                else if (this.gameBoard[row][col] == W) wPieces++;
                else hasEmpty = true;
            }
        }

        if (wPieces == 0 || bPieces == 0) return true;
        if (!hasEmpty) return true;
        if (getAvailableMoves(B).isEmpty() && getAvailableMoves(W).isEmpty()) return true;

        return false;
    }

    public int evaluate() {
        int scoreB = 0;
        int scoreW = 0;
        int sumB = 0;
        int sumW = 0;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (this.gameBoard[row][col] == W) sumW++;
                else if (this.gameBoard[row][col] == B) sumB++;
            }
        }

        // Center 2x2
        int centerW = 0;
        if (gameBoard[3][3] == W) centerW++;
        if (gameBoard[3][4] == W) centerW++;
        if (gameBoard[4][3] == W) centerW++;
        if (gameBoard[4][4] == W) centerW++;
        int centerB = 4 - centerW;
        scoreW -= centerW * 10;
        scoreB += centerB * 10;

        // Center 4x4
        centerW = 0;
        centerB = 0;
        for (int i = 2; i <= 5; i++) {
            for (int j = 2; j <= 5; j++) {
                if (gameBoard[i][j] == W) centerW++;
                else if (gameBoard[i][j] == B) centerB++;
            }
        }
        scoreW -= centerW * 10;
        scoreB += centerB * 10;

        // Corners
        int cornerW = 0;
        int cornerB = 0;
        if (gameBoard[0][0] == W) cornerW++; else if (gameBoard[0][0] == B) cornerB++;
        if (gameBoard[0][7] == W) cornerW++; else if (gameBoard[0][7] == B) cornerB++;
        if (gameBoard[7][0] == W) cornerW++; else if (gameBoard[7][0] == B) cornerB++;
        if (gameBoard[7][7] == W) cornerW++; else if (gameBoard[7][7] == B) cornerB++;
        scoreB += cornerB * 1000;
        scoreW -= cornerW * 1000;

        // Stable discs
        scoreW -= 100 * getStableDiscs(W).size();
        scoreB += 100 * getStableDiscs(B).size();

        // Disc count by game phase
        int totalDiscCount = sumB + sumW;
        if (totalDiscCount < 58) {
            if (sumW > sumB) scoreW += 20; else scoreB -= 20;
        } else {
            if (sumW > sumB) scoreW -= 50; else scoreB += 50;
        }

        // Frontier strategy
        if (getFrontierSquares(W).size() < getFrontierSquares(B).size()) scoreW -= 75;
        else if (getFrontierSquares(B).size() < getFrontierSquares(W).size()) scoreB += 75;

        // Mobility
        int mobilityDiff = getAvailableMoves(W).size() - getAvailableMoves(B).size();
        if (mobilityDiff > 0) scoreW -= 100;
        else if (mobilityDiff < 0) scoreB += 100;

        // Wedge strategy for all 4 edges
        int[] wedgeScores = applyWedgeScore(getEdgeWedges(true, 0));
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];
        wedgeScores = applyWedgeScore(getEdgeWedges(true, 7));
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];
        wedgeScores = applyWedgeScore(getEdgeWedges(false, 0));
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];
        wedgeScores = applyWedgeScore(getEdgeWedges(false, 7));
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];

        // Parity
        if (totalDiscCount >= 20) {
            int row = this.lastMove.getRow();
            int col = this.lastMove.getCol();
            if (row >= 0 && col >= 0 && row < SIZE && col < SIZE) {
                int emptyAround = countEmptyNeighbors(row, col);
                if (emptyAround % 2 == 0) {
                    if (this.lastMove.getValue() == B) { scoreB += 50; scoreW += 50; }
                    else if (this.lastMove.getValue() == W) { scoreB -= 50; scoreW -= 50; }
                } else {
                    if (this.lastMove.getValue() == B) { scoreB -= 50; scoreW -= 50; }
                    else if (this.lastMove.getValue() == W) { scoreB += 50; scoreW += 50; }
                }
            }
        }

        // X-square avoidance
        if (gameBoard[0][0] == EMPTY && gameBoard[1][1] == B) scoreB -= 25;
        else if (gameBoard[0][0] == EMPTY && gameBoard[1][1] == W) scoreW += 25;
        if (gameBoard[0][7] == EMPTY && gameBoard[1][6] == B) scoreB -= 25;
        else if (gameBoard[0][7] == EMPTY && gameBoard[1][6] == W) scoreW += 25;
        if (gameBoard[7][0] == EMPTY && gameBoard[6][1] == B) scoreB -= 25;
        else if (gameBoard[7][0] == EMPTY && gameBoard[6][1] == W) scoreW += 25;
        if (gameBoard[7][7] == EMPTY && gameBoard[6][6] == B) scoreB -= 25;
        else if (gameBoard[7][7] == EMPTY && gameBoard[6][6] == W) scoreW += 25;

        // Unbalanced edges
        int[] edgeScores = calcUnbalancedEdges();
        scoreW += edgeScores[0];
        scoreB += edgeScores[1];

        return scoreB - scoreW;
    }

    private int countEmptyNeighbors(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = row + dr;
                int c = col + dc;
                if (r >= 0 && r < SIZE && c >= 0 && c < SIZE && gameBoard[r][c] == EMPTY) {
                    count++;
                }
            }
        }
        return count;
    }

    private int[] applyWedgeScore(List<List<Integer>> wedges) {
        int sW = 0;
        int sB = 0;
        for (int val : wedges.get(0)) {
            if (val != 0) sW += (val % 2 == 0) ? -60 : 60;
        }
        for (int val : wedges.get(1)) {
            if (val != 0) sB += (val % 2 == 0) ? 60 : -60;
        }
        return new int[]{sW, sB};
    }

    private int[] calcUnbalancedEdges() {
        int scoreB = 0;
        int scoreW = 0;
        scoreB += calcEdgeBalance(0, true, B);
        scoreW += calcEdgeBalance(0, true, W);
        scoreB += calcEdgeBalance(7, true, B);
        scoreW += calcEdgeBalance(7, true, W);
        scoreB += calcEdgeBalance(0, false, B);
        scoreW += calcEdgeBalance(0, false, W);
        scoreB += calcEdgeBalance(7, false, B);
        scoreW += calcEdgeBalance(7, false, W);
        return new int[]{scoreW, scoreB};
    }

    private int calcEdgeBalance(int idx, boolean isRow, int player) {
        int opponent = player * -1;
        boolean found = false;
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == player) { found = true; count++; }
            if (val == opponent) return 0;
        }
        if (!found || count <= 1) return 0;

        int left = 0;
        for (int i = 0; i < SIZE; i++) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == EMPTY) left++; else break;
        }
        int right = 0;
        for (int i = SIZE - 1; i >= 0; i--) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == EMPTY) right++; else break;
        }

        boolean balanced = (left == right);
        if (player == B) return balanced ? 10 : -10;
        else return balanced ? -10 : 10;
    }

    public void flipDiscs(Move move) {
        int row = move.getRow();
        int col = move.getCol();
        int letter = move.getValue();
        flipDirection(row, col, -1, 0, letter);
        flipDirection(row, col, 1, 0, letter);
        flipDirection(row, col, 0, -1, letter);
        flipDirection(row, col, 0, 1, letter);
        flipDirection(row, col, -1, 1, letter);
        flipDirection(row, col, 1, 1, letter);
        flipDirection(row, col, -1, -1, letter);
        flipDirection(row, col, 1, -1, letter);
    }

    private void flipDirection(int row, int col, int dRow, int dCol, int letter) {
        int i = row + dRow;
        int j = col + dCol;
        int endI = -1;
        int endJ = -1;

        while (i >= 0 && i < SIZE && j >= 0 && j < SIZE) {
            if (gameBoard[i][j] == EMPTY) break;
            if (gameBoard[i][j] == letter) {
                endI = i;
                endJ = j;
                break;
            }
            i += dRow;
            j += dCol;
        }

        if (endI < 0) return;

        i = row + dRow;
        j = col + dCol;
        while (i != endI || j != endJ) {
            gameBoard[i][j] = letter;
            i += dRow;
            j += dCol;
        }
    }

    public List<Move> getStableDiscs(int playerLetter) {
        List<Move> stableDiscs = new ArrayList<>();

        checkCornerStability(stableDiscs, playerLetter, 0, 0, 1, 1);
        checkCornerStability(stableDiscs, playerLetter, 0, 7, 1, -1);
        checkCornerStability(stableDiscs, playerLetter, 7, 0, -1, 1);
        checkCornerStability(stableDiscs, playerLetter, 7, 7, -1, -1);

        return removeDuplicateMoves(stableDiscs);
    }

    private void checkCornerStability(List<Move> list, int player, int cornerRow, int cornerCol, int rowDir, int colDir) {
        if (gameBoard[cornerRow][cornerCol] != player) return;

        list.add(new Move(cornerRow, cornerCol));

        // Stable discs along the row from corner
        int c = cornerCol + colDir;
        while (c >= 0 && c < SIZE && gameBoard[cornerRow][c] == player) {
            list.add(new Move(cornerRow, c));
            c += colDir;
        }

        // Stable discs along the column from corner
        int r = cornerRow + rowDir;
        while (r >= 0 && r < SIZE && gameBoard[r][cornerCol] == player) {
            list.add(new Move(r, cornerCol));
            r += rowDir;
        }

        // Diagonal stable discs
        int rStart = cornerRow + rowDir;
        int rEnd = (rowDir > 0) ? SIZE - 1 : 1;
        int cStart = cornerCol + colDir;
        int cEnd = (colDir > 0) ? SIZE - 1 : 1;

        for (r = rStart; (rowDir > 0 ? r <= rEnd : r >= rEnd); r += rowDir) {
            for (c = cStart; (colDir > 0 ? c <= cEnd : c >= cEnd); c += colDir) {
                if (gameBoard[r][c] != player) break;
                if (gameBoard[r - rowDir][c] != player || gameBoard[r][c - colDir] != player) break;
                int diagR = r - rowDir;
                int diagC = c + colDir;
                int antiDiagR = r + rowDir;
                int antiDiagC = c - colDir;
                boolean diagOk = (diagR >= 0 && diagR < SIZE && diagC >= 0 && diagC < SIZE && gameBoard[diagR][diagC] == player);
                boolean antiDiagOk = (antiDiagR >= 0 && antiDiagR < SIZE && antiDiagC >= 0 && antiDiagC < SIZE && gameBoard[antiDiagR][antiDiagC] == player);
                if (diagOk || antiDiagOk) {
                    list.add(new Move(r, c));
                } else {
                    break;
                }
            }
        }
    }

    public List<Move> getAvailableMoves(int playerLetter) {
        List<Move> moves = new ArrayList<>();
        List<Move> opponentFrontier = getFrontierSquares(-playerLetter);

        for (Move frontier : opponentFrontier) {
            checkDirection(moves, frontier, playerLetter, -1, 0);
            checkDirection(moves, frontier, playerLetter, 1, 0);
            checkDirection(moves, frontier, playerLetter, 0, 1);
            checkDirection(moves, frontier, playerLetter, 0, -1);
            checkDiagDirection(moves, frontier, playerLetter, -1, -1);
            checkDiagDirection(moves, frontier, playerLetter, -1, 1);
            checkDiagDirection(moves, frontier, playerLetter, 1, -1);
            checkDiagDirection(moves, frontier, playerLetter, 1, 1);
        }

        return removeDuplicateMoves(moves);
    }

    private void checkDirection(List<Move> moves, Move frontier, int playerLetter, int dRow, int dCol) {
        int fRow = frontier.getRow();
        int fCol = frontier.getCol();

        if (dRow != 0) {
            if (dRow < 0 && fRow <= 0) return;
            if (dRow > 0 && fRow >= SIZE - 1) return;
            if (dRow < 0 && fRow >= SIZE - 1) return;
        }
        if (dCol != 0) {
            if (dCol < 0 && fCol <= 0) return;
            if (dCol > 0 && fCol >= SIZE - 1) return;
        }

        boolean ourDiscExists = false;
        int r = fRow + dRow;
        int c = fCol + dCol;
        while (r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
            if (gameBoard[r][c] == playerLetter) { ourDiscExists = true; break; }
            else if (gameBoard[r][c] == EMPTY) break;
            r += dRow;
            c += dCol;
        }

        if (ourDiscExists) {
            r = fRow - dRow;
            c = fCol - dCol;
            while (r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
                if (gameBoard[r][c] == EMPTY) {
                    moves.add(new Move(r, c));
                    return;
                }
                if (gameBoard[r][c] == playerLetter) return;
                r -= dRow;
                c -= dCol;
            }
        }
    }

    private void checkDiagDirection(List<Move> moves, Move frontier, int playerLetter, int dRow, int dCol) {
        int fRow = frontier.getRow();
        int fCol = frontier.getCol();

        if (fRow <= 0 || fRow >= SIZE - 1 || fCol <= 0 || fCol >= SIZE - 1) return;

        boolean ourDiscExists = false;
        boolean blocked = false;
        int r = fRow + dRow;
        int c = fCol + dCol;

        while (r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
            if (gameBoard[r][c] == playerLetter) { ourDiscExists = true; break; }
            else if (gameBoard[r][c] == EMPTY) { blocked = true; break; }
            r += dRow;
            c += dCol;
        }

        if (ourDiscExists && !blocked) {
            r = fRow - dRow;
            c = fCol - dCol;
            while (r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
                if (gameBoard[r][c] == EMPTY) {
                    moves.add(new Move(r, c));
                    return;
                }
                if (gameBoard[r][c] == playerLetter) return;
                r -= dRow;
                c -= dCol;
            }
        }
    }

    public List<Move> getFrontierSquares(int playerLetter) {
        List<Move> frontiers = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (gameBoard[i][j] != playerLetter) continue;
                if (hasEmptyNeighbor(i, j)) {
                    frontiers.add(new Move(i, j));
                }
            }
        }

        List<Move> stableDiscs = getStableDiscs(playerLetter);
        frontiers.removeIf(fd -> stableDiscs.stream()
                .anyMatch(sd -> fd.getRow() == sd.getRow() && fd.getCol() == sd.getCol()));

        return removeDuplicateMoves(frontiers);
    }

    private boolean hasEmptyNeighbor(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = row + dr;
                int c = col + dc;
                if (r >= 0 && r < SIZE && c >= 0 && c < SIZE && gameBoard[r][c] == EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Move> removeDuplicateMoves(List<Move> moves) {
        List<Move> unique = new ArrayList<>();
        for (Move m : moves) {
            boolean dup = false;
            for (Move u : unique) {
                if (m.getRow() == u.getRow() && m.getCol() == u.getCol()) { dup = true; break; }
            }
            if (!dup) unique.add(m);
        }
        return unique;
    }

    private List<List<Integer>> getEdgeWedges(boolean isRow, int idx) {
        List<Integer> whiteGaps = new ArrayList<>();
        List<Integer> blackGaps = new ArrayList<>();
        int emptyCountB = 0;
        int emptyCountW = 0;

        for (int j = 0; j < SIZE; j++) {
            int val = isRow ? gameBoard[idx][j] : gameBoard[j][idx];
            if (val == B) {
                int k = j + 1;
                for (; k < SIZE; k++) {
                    int v = isRow ? gameBoard[idx][k] : gameBoard[k][idx];
                    if (v == EMPTY) emptyCountB++;
                    else {
                        if (v == B && emptyCountB > 0 && k != 0) blackGaps.add(emptyCountB);
                        emptyCountB = 0;
                        break;
                    }
                }
                if (k == SIZE) { emptyCountB = 0; break; }
            }
            if (val == W) {
                int k = j + 1;
                for (; k < SIZE; k++) {
                    int v = isRow ? gameBoard[idx][k] : gameBoard[k][idx];
                    if (v == EMPTY) emptyCountW++;
                    else {
                        if (v == W && emptyCountW > 0 && k != 0) whiteGaps.add(emptyCountW);
                        emptyCountW = 0;
                        break;
                    }
                }
                if (k == SIZE) { emptyCountW = 0; break; }
            }
        }

        List<List<Integer>> wedges = new ArrayList<>();
        wedges.add(whiteGaps);
        wedges.add(blackGaps);
        return wedges;
    }

    public Move getLastMove() { return this.lastMove; }
    public int getLastPlayer() { return this.lastPlayer; }
    public int[][] getGameBoard() { return this.gameBoard; }
    public void setLastPlayer(int lastPlayer) { this.lastPlayer = lastPlayer; }
}
