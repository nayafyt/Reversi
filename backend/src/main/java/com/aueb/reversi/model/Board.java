package com.aueb.reversi.model;

import java.util.ArrayList;

public class Board {
    public static final int W = 1;
    public static final int B = -1;
    public static final int EMPTY = 0;

    private int[][] gameBoard;
    private int lastPlayer;
    private Move lastMove;
    private int dimension = 8;

    public Board() {
        this.lastMove = new Move();
        this.lastPlayer = W;
        this.gameBoard = new int[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                this.gameBoard[i][j] = EMPTY;
        this.gameBoard[3][3] = W;
        this.gameBoard[4][4] = W;
        this.gameBoard[3][4] = B;
        this.gameBoard[4][3] = B;
    }

    public Board(Board board) {
        this.lastMove = new Move(board.lastMove.getRow(), board.lastMove.getCol(), board.lastMove.getValue());
        this.lastPlayer = board.lastPlayer;
        this.gameBoard = new int[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                this.gameBoard[i][j] = board.gameBoard[i][j];
    }

    public void makeMove(int row, int col, int letter) {
        this.gameBoard[row][col] = letter;
        this.lastMove = new Move(row, col);
        this.lastPlayer = letter;
    }

    public ArrayList<Board> getChildren(int letter) {
        ArrayList<Board> children = new ArrayList<>();
        ArrayList<Move> moves = getavailableMoves(letter);
        for (Move mv : moves) {
            Board child = new Board(this);
            int row = mv.getRow();
            int col = mv.getCol();
            child.makeMove(row, col, letter);
            child.switcher(new Move(row, col, letter));
            children.add(child);
        }
        return children;
    }

    public boolean isTerminal() {
        // Check if all pieces are one color
        int bPieces = 0;
        int wPieces = 0;
        boolean hasEmpty = false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (this.gameBoard[row][col] == B) bPieces++;
                else if (this.gameBoard[row][col] == W) wPieces++;
                else hasEmpty = true;
            }
        }

        if (wPieces == 0 || bPieces == 0) return true;
        if (!hasEmpty) return true;

        // Both players have no moves
        if (getavailableMoves(B).isEmpty() && getavailableMoves(W).isEmpty()) return true;

        return false;
    }

    public int evaluate() {
        int scoreB = 0;
        int scoreW = 0;
        int sumB = 0;
        int sumW = 0;
        int centerW = 0;
        int centerB = 0;
        int cornerW = 0;
        int cornerB = 0;

        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                if (this.gameBoard[row][col] == W) sumW++;
                else if (this.gameBoard[row][col] == B) sumB++;
            }
        }

        // Center 2x2
        if (gameBoard[3][3] == W) centerW++;
        if (gameBoard[3][4] == W) centerW++;
        if (gameBoard[4][3] == W) centerW++;
        if (gameBoard[4][4] == W) centerW++;
        centerB = 4 - centerW;
        scoreW -= centerW * 10;
        scoreB += centerB * 10;
        centerW = 0;
        centerB = 0;

        // Center 4x4
        for (int i = 2; i <= 5; i++) {
            for (int j = 2; j <= 5; j++) {
                if (gameBoard[i][j] == W) centerW++;
                else if (gameBoard[i][j] == B) centerB++;
            }
        }
        scoreW -= centerW * 10;
        scoreB += centerB * 10;

        // Corners
        if (gameBoard[0][0] == W) cornerW++; else if (gameBoard[0][0] == B) cornerB++;
        if (gameBoard[0][7] == W) cornerW++; else if (gameBoard[0][7] == B) cornerB++;
        if (gameBoard[7][0] == W) cornerW++; else if (gameBoard[7][0] == B) cornerB++;
        if (gameBoard[7][7] == W) cornerW++; else if (gameBoard[7][7] == B) cornerB++;
        scoreB += cornerB * 1000;
        scoreW -= cornerW * 1000;

        // Stable discs
        int stableDiscs_W = getStableDiscs(W).size();
        int stableDiscs_B = getStableDiscs(B).size();
        scoreW -= 100 * stableDiscs_W;
        scoreB += 100 * stableDiscs_B;

        int totalDiscCnt = sumB + sumW;
        if (totalDiscCnt < 58) {
            if (sumW > sumB) scoreW += 20;
            else scoreB -= 20;
        } else {
            if (sumW > sumB) scoreW -= 50;
            else scoreB += 50;
        }

        // Frontier strategy
        if (getFrontierSquares(W).size() < getFrontierSquares(B).size()) scoreW -= 75;
        else if (getFrontierSquares(B).size() < getFrontierSquares(W).size()) scoreB += 75;

        // Mobility
        int movesW = getavailableMoves(W).size();
        int movesB = getavailableMoves(B).size();
        int diafora = movesW - movesB;
        if (diafora > 0) scoreW -= 100;
        else if (diafora < 0) scoreB += 100;

        // Wedge strategy
        applyWedgeScore(getUpWedges(), scoreW, scoreB);
        int[] wedgeScores = applyWedgeScore(getUpWedges(), 0, 0);
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];
        wedgeScores = applyWedgeScore(getLeftWedges(), 0, 0);
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];
        wedgeScores = applyWedgeScore(getRightWedges(), 0, 0);
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];
        wedgeScores = applyWedgeScore(getDownWedges(), 0, 0);
        scoreW += wedgeScores[0]; scoreB += wedgeScores[1];

        // Parity
        if (totalDiscCnt >= 20) {
            int emptyaround = 0;
            int row = this.lastMove.getRow();
            int col = this.lastMove.getCol();
            if (row >= 0 && col >= 0 && row < 8 && col < 8) {
                if (row >= 1 && col >= 1 && gameBoard[row-1][col-1] == EMPTY) emptyaround++;
                if (row >= 1 && gameBoard[row-1][col] == EMPTY) emptyaround++;
                if (row >= 1 && col < 7 && gameBoard[row-1][col+1] == EMPTY) emptyaround++;
                if (col < 7 && gameBoard[row][col+1] == EMPTY) emptyaround++;
                if (col >= 1 && gameBoard[row][col-1] == EMPTY) emptyaround++;
                if (row < 7 && col >= 1 && gameBoard[row+1][col-1] == EMPTY) emptyaround++;
                if (row < 7 && gameBoard[row+1][col] == EMPTY) emptyaround++;
                if (row < 7 && col < 7 && gameBoard[row+1][col+1] == EMPTY) emptyaround++;

                if (emptyaround % 2 == 0) {
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
        ArrayList<Integer> balanced = unbalancedEdges();
        scoreW += balanced.get(0);
        scoreB += balanced.get(1);

        return scoreB - scoreW;
    }

    private int[] applyWedgeScore(ArrayList<ArrayList<Integer>> wedges, int sW, int sB) {
        for (int j = 0; j < wedges.get(0).size(); j++) {
            if (wedges.get(0).get(j) != 0 && wedges.get(0).get(j) % 2 == 0) sW -= 60;
            else if (wedges.get(0).get(j) != 0 && wedges.get(0).get(j) % 2 != 0) sW += 60;
        }
        for (int j = 0; j < wedges.get(1).size(); j++) {
            if (wedges.get(1).get(j) != 0 && wedges.get(1).get(j) % 2 == 0) sB += 60;
            else if (wedges.get(1).get(j) != 0 && wedges.get(1).get(j) % 2 != 0) sB -= 60;
        }
        return new int[]{sW, sB};
    }

    private ArrayList<Integer> unbalancedEdges() {
        ArrayList<Integer> balanced = new ArrayList<>();
        int scoreb = 0, scorew = 0;

        scoreb += calcEdgeBalance(0, true);
        scorew += calcEdgeBalanceW(0, true);
        scoreb += calcEdgeBalance(7, true);
        scorew += calcEdgeBalanceW(7, true);
        scoreb += calcEdgeBalance(0, false);
        scorew += calcEdgeBalanceW(0, false);
        scoreb += calcEdgeBalance(7, false);
        scorew += calcEdgeBalanceW(7, false);

        balanced.add(scorew);
        balanced.add(scoreb);
        return balanced;
    }

    private int calcEdgeBalance(int idx, boolean isRow) {
        boolean found = false;
        int count = 0;
        for (int i = 0; i < dimension; i++) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == B) { found = true; count++; }
            int valOther = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (valOther == W) return 0; // mixed edge
        }
        if (!found || count <= 1) return 0;

        int left = 0, right = 0;
        for (int i = 0; i < dimension; i++) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == EMPTY) left++; else break;
        }
        for (int i = dimension - 1; i >= 0; i--) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == EMPTY) right++; else break;
        }
        return left == right ? 10 : -10;
    }

    private int calcEdgeBalanceW(int idx, boolean isRow) {
        boolean found = false;
        int count = 0;
        for (int i = 0; i < dimension; i++) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == W) { found = true; count++; }
            int valOther = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (valOther == B) return 0;
        }
        if (!found || count <= 1) return 0;

        int left = 0, right = 0;
        for (int i = 0; i < dimension; i++) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == EMPTY) left++; else break;
        }
        for (int i = dimension - 1; i >= 0; i--) {
            int val = isRow ? gameBoard[idx][i] : gameBoard[i][idx];
            if (val == EMPTY) right++; else break;
        }
        return left == right ? -10 : 10;
    }

    public int switcher(Move move) {
        int row = move.getRow();
        int col = move.getCol();

        // 1. up
        flipDirection(row, col, -1, 0, move.getValue());
        // 2. down
        flipDirection(row, col, 1, 0, move.getValue());
        // 3. left
        flipDirection(row, col, 0, -1, move.getValue());
        // 4. right
        flipDirection(row, col, 0, 1, move.getValue());
        // 5. up-right
        flipDirection(row, col, -1, 1, move.getValue());
        // 6. down-right
        flipDirection(row, col, 1, 1, move.getValue());
        // 7. up-left
        flipDirection(row, col, -1, -1, move.getValue());
        // 8. down-left
        flipDirection(row, col, 1, -1, move.getValue());

        return 0;
    }

    private void flipDirection(int row, int col, int dRow, int dCol, int letter) {
        int i = row + dRow;
        int j = col + dCol;
        boolean found = false;
        int endI = -1, endJ = -1;

        while (i >= 0 && i <= 7 && j >= 0 && j <= 7) {
            if (gameBoard[i][j] == EMPTY) break;
            if (gameBoard[i][j] == letter) {
                found = true;
                endI = i;
                endJ = j;
                break;
            }
            i += dRow;
            j += dCol;
        }

        if (found) {
            i = row + dRow;
            j = col + dCol;
            while (i != endI || j != endJ) {
                if (gameBoard[i][j] == letter * (-1)) {
                    gameBoard[i][j] = letter;
                }
                i += dRow;
                j += dCol;
            }
        }
    }

    public ArrayList<Move> getStableDiscs(int playerLetter) {
        ArrayList<Move> stablediscs = new ArrayList<>();
        int sd = 0;

        // Left top corner
        if (gameBoard[0][0] == playerLetter) {
            sd++;
            stablediscs.add(new Move(0, 0));
            for (int col = 1; col < 8; col++) {
                if (gameBoard[0][col] == playerLetter) { sd++; stablediscs.add(new Move(0, col)); }
                else break;
            }
            for (int r = 1; r < 8; r++) {
                if (gameBoard[r][0] == playerLetter) { sd++; stablediscs.add(new Move(r, 0)); }
                else break;
            }
            if (sd > 0) addDiagonalStable(stablediscs, playerLetter, 1, 7, 1, 7, 1, 1, -1, 1, 1, -1);
        }

        // Right top corner
        if (gameBoard[0][7] == playerLetter) {
            sd++;
            stablediscs.add(new Move(0, 7));
            for (int col = 6; col >= 0; col--) {
                if (gameBoard[0][col] == playerLetter) { sd++; stablediscs.add(new Move(0, col)); }
                else break;
            }
            for (int r = 1; r < 8; r++) {
                if (gameBoard[r][7] == playerLetter) { sd++; stablediscs.add(new Move(r, 7)); }
                else break;
            }
            if (sd > 0) addDiagonalStable2(stablediscs, playerLetter, 1, 7, 6, 0, 1, -1, -1, -1, 1, 1);
        }

        // Left bottom corner
        if (gameBoard[7][0] == playerLetter) {
            sd++;
            stablediscs.add(new Move(7, 0));
            for (int col = 1; col < 8; col++) {
                if (gameBoard[7][col] == playerLetter) { sd++; stablediscs.add(new Move(7, col)); }
                else break;
            }
            for (int r = 6; r >= 0; r--) {
                if (gameBoard[r][0] == playerLetter) { sd++; stablediscs.add(new Move(r, 0)); }
                else break;
            }
            if (sd > 0) addDiagonalStable3(stablediscs, playerLetter, 6, 0, 1, 7, -1, 1, -1, -1, 1, 1);
        }

        // Right bottom corner
        if (gameBoard[7][7] == playerLetter) {
            sd++;
            stablediscs.add(new Move(7, 7));
            for (int col = 6; col >= 0; col--) {
                if (gameBoard[7][col] == playerLetter) { sd++; stablediscs.add(new Move(7, col)); }
                else break;
            }
            for (int r = 6; r >= 0; r--) {
                if (gameBoard[r][7] == playerLetter) { sd++; stablediscs.add(new Move(r, 7)); }
                else break;
            }
            if (sd > 0) addDiagonalStable4(stablediscs, playerLetter, 6, 0, 6, 0, -1, -1, 1, -1, -1, 1);
        }

        // Remove duplicates
        return removeDuplicateMoves(stablediscs);
    }

    private void addDiagonalStable(ArrayList<Move> list, int pl, int rStart, int rEnd, int cStart, int cEnd, int rDir, int cDir, int adjRow, int adjCol, int diagRow, int diagCol) {
        for (int r = rStart; r < rEnd; r++) {
            for (int c = cStart; c < cEnd; c++) {
                if (gameBoard[r][c] == pl && gameBoard[r-1][c] == pl && gameBoard[r][c-1] == pl) {
                    if (gameBoard[r-1][c+1] == pl || gameBoard[r+1][c-1] == pl) {
                        list.add(new Move(r, c));
                    } else break;
                } else break;
            }
        }
    }

    private void addDiagonalStable2(ArrayList<Move> list, int pl, int rStart, int rEnd, int cStart, int cEnd, int rDir, int cDir, int adjRow, int adjCol, int diagRow, int diagCol) {
        for (int r = rStart; r < rEnd; r++) {
            for (int c = cStart; c > cEnd; c--) {
                if (gameBoard[r][c] == pl && gameBoard[r-1][c] == pl && gameBoard[r][c+1] == pl) {
                    if (gameBoard[r-1][c-1] == pl || gameBoard[r+1][c+1] == pl) {
                        list.add(new Move(r, c));
                    } else break;
                } else break;
            }
        }
    }

    private void addDiagonalStable3(ArrayList<Move> list, int pl, int rStart, int rEnd, int cStart, int cEnd, int rDir, int cDir, int adjRow, int adjCol, int diagRow, int diagCol) {
        for (int r = rStart; r > rEnd; r--) {
            for (int c = cStart; c < cEnd; c++) {
                if (gameBoard[r][c] == pl && gameBoard[r+1][c] == pl && gameBoard[r][c-1] == pl) {
                    if (gameBoard[r-1][c-1] == pl || gameBoard[r+1][c+1] == pl) {
                        list.add(new Move(r, c));
                    } else break;
                } else break;
            }
        }
    }

    private void addDiagonalStable4(ArrayList<Move> list, int pl, int rStart, int rEnd, int cStart, int cEnd, int rDir, int cDir, int adjRow, int adjCol, int diagRow, int diagCol) {
        for (int r = rStart; r > rEnd; r--) {
            for (int c = cStart; c > cEnd; c--) {
                if (gameBoard[r][c] == pl && gameBoard[r+1][c] == pl && gameBoard[r][c+1] == pl) {
                    if (gameBoard[r-1][c+1] == pl || gameBoard[r+1][c-1] == pl) {
                        list.add(new Move(r, c));
                    } else break;
                } else break;
            }
        }
    }

    public ArrayList<Move> getavailableMoves(int playerLetter) {
        ArrayList<Move> rc = new ArrayList<>();
        ArrayList<Move> frontiersOpponent = getFrontierSquares((-1) * playerLetter);

        for (Move frontOpMove : frontiersOpponent) {
            checkDirection(rc, frontOpMove, playerLetter, -1, 0);  // up
            checkDirection(rc, frontOpMove, playerLetter, 1, 0);   // down
            checkDirection(rc, frontOpMove, playerLetter, 0, 1);   // right
            checkDirection(rc, frontOpMove, playerLetter, 0, -1);  // left
            checkDiagDirection(rc, frontOpMove, playerLetter, -1, -1); // left-up
            checkDiagDirection(rc, frontOpMove, playerLetter, -1, 1);  // right-up
            checkDiagDirection(rc, frontOpMove, playerLetter, 1, -1);  // left-down
            checkDiagDirection(rc, frontOpMove, playerLetter, 1, 1);   // right-down
        }

        return removeDuplicateMoves(rc);
    }

    private void checkDirection(ArrayList<Move> rc, Move front, int playerLetter, int dRow, int dCol) {
        int fRow = front.getRow();
        int fCol = front.getCol();

        // Check boundary for the search direction
        if (dRow != 0) {
            if (dRow < 0 && fRow <= 0) return;
            if (dRow > 0 && fRow >= 7) return;
            if (dRow < 0 && fRow >= 7) return; // need room for empty space on other side
        }
        if (dCol != 0) {
            if (dCol < 0 && fCol <= 0) return;
            if (dCol > 0 && fCol >= 7) return;
        }

        // Search for our disc in the given direction
        boolean ourDiscExists = false;
        int r = fRow + dRow;
        int c = fCol + dCol;
        while (r >= 0 && r <= 7 && c >= 0 && c <= 7) {
            if (gameBoard[r][c] == playerLetter) { ourDiscExists = true; break; }
            else if (gameBoard[r][c] == EMPTY) break;
            r += dRow;
            c += dCol;
        }

        if (ourDiscExists) {
            // Search for empty space in the opposite direction
            r = fRow - dRow;
            c = fCol - dCol;
            while (r >= 0 && r <= 7 && c >= 0 && c <= 7) {
                if (gameBoard[r][c] == EMPTY) {
                    rc.add(new Move(r, c));
                    return;
                }
                if (gameBoard[r][c] == playerLetter) return;
                r -= dRow;
                c -= dCol;
            }
        }
    }

    private void checkDiagDirection(ArrayList<Move> rc, Move front, int playerLetter, int dRow, int dCol) {
        int fRow = front.getRow();
        int fCol = front.getCol();

        if (fRow <= 0 || fRow >= 7 || fCol <= 0 || fCol >= 7) return;

        boolean ourDiscExists = false;
        boolean cantplay = false;
        int r = fRow + dRow;
        int c = fCol + dCol;

        while (r >= 0 && r <= 7 && c >= 0 && c <= 7) {
            if (gameBoard[r][c] == playerLetter) { ourDiscExists = true; break; }
            else if (gameBoard[r][c] == EMPTY) { cantplay = true; break; }
            r += dRow;
            c += dCol;
        }

        if (ourDiscExists && !cantplay) {
            r = fRow - dRow;
            c = fCol - dCol;
            while (r >= 0 && r <= 7 && c >= 0 && c <= 7) {
                if (gameBoard[r][c] == EMPTY) {
                    rc.add(new Move(r, c));
                    return;
                }
                if (gameBoard[r][c] == playerLetter) return;
                r -= dRow;
                c -= dCol;
            }
        }
    }

    public ArrayList<Move> getFrontierSquares(int playerletter) {
        ArrayList<Move> frontiers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (gameBoard[i][j] == playerletter) {
                    boolean isFrontier = false;
                    if (i > 0 && gameBoard[i-1][j] == 0) isFrontier = true;
                    if (i < 7 && gameBoard[i+1][j] == 0) isFrontier = true;
                    if (j < 7 && gameBoard[i][j+1] == 0) isFrontier = true;
                    if (j > 0 && gameBoard[i][j-1] == 0) isFrontier = true;
                    if (i > 0 && j > 0 && gameBoard[i-1][j-1] == 0) isFrontier = true;
                    if (i > 0 && j < 7 && gameBoard[i-1][j+1] == 0) isFrontier = true;
                    if (i < 7 && j > 0 && gameBoard[i+1][j-1] == 0) isFrontier = true;
                    if (i < 7 && j < 7 && gameBoard[i+1][j+1] == 0) isFrontier = true;
                    if (isFrontier) frontiers.add(new Move(i, j));
                }
            }
        }

        // Remove stable discs from frontiers
        ArrayList<Move> stableDiscs = getStableDiscs(playerletter);
        frontiers.removeIf(fd -> {
            for (Move sd : stableDiscs) {
                if (fd.getRow() == sd.getRow() && fd.getCol() == sd.getCol()) return true;
            }
            return false;
        });

        return removeDuplicateMoves(frontiers);
    }

    private ArrayList<Move> removeDuplicateMoves(ArrayList<Move> moves) {
        ArrayList<Move> unique = new ArrayList<>();
        for (Move m : moves) {
            boolean dup = false;
            for (Move u : unique) {
                if (m.getRow() == u.getRow() && m.getCol() == u.getCol()) { dup = true; break; }
            }
            if (!dup) unique.add(m);
        }
        return unique;
    }

    // Wedge methods
    private ArrayList<ArrayList<Integer>> getUpWedges() { return getEdgeWedges(true, 0); }
    private ArrayList<ArrayList<Integer>> getDownWedges() { return getEdgeWedges(true, 7); }
    private ArrayList<ArrayList<Integer>> getLeftWedges() { return getEdgeWedges(false, 0); }
    private ArrayList<ArrayList<Integer>> getRightWedges() { return getEdgeWedges(false, 7); }

    private ArrayList<ArrayList<Integer>> getEdgeWedges(boolean isRow, int idx) {
        ArrayList<Integer> whkeno = new ArrayList<>();
        ArrayList<Integer> blkeno = new ArrayList<>();
        ArrayList<ArrayList<Integer>> wedges = new ArrayList<>();
        int empsqB = 0, empsqW = 0;

        for (int j = 0; j < dimension; j++) {
            int val = isRow ? gameBoard[idx][j] : gameBoard[j][idx];
            if (val == B) {
                int stili = j + 1;
                int k = stili;
                for (k = stili; k < dimension; k++) {
                    int v = isRow ? gameBoard[idx][k] : gameBoard[k][idx];
                    if (v == EMPTY) empsqB++;
                    else {
                        int stopVal = isRow ? gameBoard[idx][k] : gameBoard[k][idx];
                        if (stopVal == B && empsqB > 0 && k != 0) blkeno.add(empsqB);
                        empsqB = 0;
                        break;
                    }
                }
                if (k == dimension) { empsqB = 0; break; }
            }
            if (val == W) {
                int stili = j + 1;
                int k = stili;
                for (k = stili; k < dimension; k++) {
                    int v = isRow ? gameBoard[idx][k] : gameBoard[k][idx];
                    if (v == EMPTY) empsqW++;
                    else {
                        int stopVal = isRow ? gameBoard[idx][k] : gameBoard[k][idx];
                        if (stopVal == W && empsqW > 0 && k != 0) whkeno.add(empsqW);
                        empsqW = 0;
                        break;
                    }
                }
                if (k == dimension) { empsqW = 0; break; }
            }
        }
        wedges.add(whkeno);
        wedges.add(blkeno);
        return wedges;
    }

    public Move getLastMove() { return this.lastMove; }
    public int getLastPlayer() { return this.lastPlayer; }
    public int[][] getGameBoard() { return this.gameBoard; }

    public void setGameBoard(int[][] gameBoard) {
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                this.gameBoard[i][j] = gameBoard[i][j];
    }

    public void setLastMove(Move lastMove) {
        this.lastMove.setRow(lastMove.getRow());
        this.lastMove.setCol(lastMove.getCol());
        this.lastMove.setValue(lastMove.getValue());
    }

    public void setLastPlayer(int lastPlayer) { this.lastPlayer = lastPlayer; }
}
