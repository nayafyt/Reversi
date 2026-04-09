import { BOARD_SIZE, WHITE, BLACK, EMPTY, Move } from "./types";

export class Board {
    private gameBoard: number[][] = [];
    private lastPlayer: number = WHITE;
    private lastMove: Move = { row: -1, col: -1 };

    constructor(board?: Board) {
        if (board) {
            // Copy constructor
            this.lastMove = { ...board.lastMove };
            this.lastPlayer = board.lastPlayer;
            this.gameBoard = board.gameBoard.map((row) => [...row]);
        } else {
            // Initialize empty board
            this.gameBoard = Array(BOARD_SIZE)
                .fill(null)
                .map(() => Array(BOARD_SIZE).fill(EMPTY));

            // Initial position
            this.gameBoard[3][3] = WHITE;
            this.gameBoard[4][4] = WHITE;
            this.gameBoard[3][4] = BLACK;
            this.gameBoard[4][3] = BLACK;
        }
    }

    makeMove(row: number, col: number, letter: number): void {
        this.gameBoard[row][col] = letter;
        this.lastMove = { row, col, value: letter };
        this.lastPlayer = letter;
    }

    flipDiscs(move: Move): void {
        const { row, col, value: letter } = move;
        this.flipDirection(row, col, -1, 0, letter!);
        this.flipDirection(row, col, 1, 0, letter!);
        this.flipDirection(row, col, 0, -1, letter!);
        this.flipDirection(row, col, 0, 1, letter!);
        this.flipDirection(row, col, -1, 1, letter!);
        this.flipDirection(row, col, 1, 1, letter!);
        this.flipDirection(row, col, -1, -1, letter!);
        this.flipDirection(row, col, 1, -1, letter!);
    }

    private flipDirection(
        row: number,
        col: number,
        dRow: number,
        dCol: number,
        letter: number
    ): void {
        let i = row + dRow;
        let j = col + dCol;
        let endI = -1;
        let endJ = -1;

        while (i >= 0 && i < BOARD_SIZE && j >= 0 && j < BOARD_SIZE) {
            if (this.gameBoard[i][j] === EMPTY) break;
            if (this.gameBoard[i][j] === letter) {
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
        while (i !== endI || j !== endJ) {
            this.gameBoard[i][j] = letter;
            i += dRow;
            j += dCol;
        }
    }

    getAvailableMoves(letter: number): Move[] {
        const moves: Move[] = [];
        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                if (this.gameBoard[row][col] !== EMPTY) continue;
                if (this.isValidMove(row, col, letter)) {
                    moves.push({ row, col });
                }
            }
        }
        return moves;
    }

    private isValidMove(row: number, col: number, letter: number): boolean {
        const opponent = letter === WHITE ? BLACK : WHITE;
        const directions = [
            [-1, 0],
            [1, 0],
            [0, -1],
            [0, 1],
            [-1, 1],
            [1, 1],
            [-1, -1],
            [1, -1],
        ];

        for (const [dr, dc] of directions) {
            let i = row + dr;
            let j = col + dc;
            let hasOpponent = false;

            while (i >= 0 && i < BOARD_SIZE && j >= 0 && j < BOARD_SIZE) {
                if (this.gameBoard[i][j] === EMPTY) break;
                if (this.gameBoard[i][j] === opponent) {
                    hasOpponent = true;
                } else if (this.gameBoard[i][j] === letter) {
                    if (hasOpponent) return true;
                    break;
                }
                i += dr;
                j += dc;
            }
        }
        return false;
    }

    getChildren(letter: number): Board[] {
        const children: Board[] = [];
        const moves = this.getAvailableMoves(letter);
        for (const move of moves) {
            const child = new Board(this);
            child.makeMove(move.row, move.col, letter);
            child.flipDiscs({ ...move, value: letter });
            children.push(child);
        }
        return children;
    }

    isTerminal(): boolean {
        let bPieces = 0;
        let wPieces = 0;
        let hasEmpty = false;

        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                if (this.gameBoard[row][col] === BLACK) bPieces++;
                else if (this.gameBoard[row][col] === WHITE) wPieces++;
                else hasEmpty = true;
            }
        }

        if (wPieces === 0 || bPieces === 0) return true;
        if (!hasEmpty) return true;
        if (
            this.getAvailableMoves(BLACK).length === 0 &&
            this.getAvailableMoves(WHITE).length === 0
        )
            return true;

        return false;
    }

    evaluate(): number {
        let scoreB = 0;
        let scoreW = 0;
        let sumB = 0;
        let sumW = 0;

        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                if (this.gameBoard[row][col] === WHITE) sumW++;
                else if (this.gameBoard[row][col] === BLACK) sumB++;
            }
        }

        // Center 2x2
        let centerW = 0;
        if (this.gameBoard[3][3] === WHITE) centerW++;
        if (this.gameBoard[3][4] === WHITE) centerW++;
        if (this.gameBoard[4][3] === WHITE) centerW++;
        if (this.gameBoard[4][4] === WHITE) centerW++;
        const centerB = 4 - centerW;
        scoreW -= centerW * 10;
        scoreB += centerB * 10;

        // Center 4x4
        let centerW4 = 0;
        let centerB4 = 0;
        for (let i = 2; i <= 5; i++) {
            for (let j = 2; j <= 5; j++) {
                if (this.gameBoard[i][j] === WHITE) centerW4++;
                else if (this.gameBoard[i][j] === BLACK) centerB4++;
            }
        }
        scoreW -= centerW4 * 10;
        scoreB += centerB4 * 10;

        // Corners
        let cornerW = 0;
        let cornerB = 0;
        if (this.gameBoard[0][0] === WHITE) cornerW++;
        else if (this.gameBoard[0][0] === BLACK) cornerB++;
        if (this.gameBoard[0][7] === WHITE) cornerW++;
        else if (this.gameBoard[0][7] === BLACK) cornerB++;
        if (this.gameBoard[7][0] === WHITE) cornerW++;
        else if (this.gameBoard[7][0] === BLACK) cornerB++;
        if (this.gameBoard[7][7] === WHITE) cornerW++;
        else if (this.gameBoard[7][7] === BLACK) cornerB++;
        scoreB += cornerB * 1000;
        scoreW -= cornerW * 1000;

        // Frontier strategy
        const frontierW = this.getFrontierSquares(WHITE).length;
        const frontierB = this.getFrontierSquares(BLACK).length;
        if (frontierW < frontierB) scoreW -= 75;
        else if (frontierB < frontierW) scoreB += 75;

        // Mobility
        const mobilityDiff =
            this.getAvailableMoves(WHITE).length -
            this.getAvailableMoves(BLACK).length;
        if (mobilityDiff > 0) scoreW -= 100;
        else if (mobilityDiff < 0) scoreB += 100;

        // X-square avoidance
        if (
            this.gameBoard[0][0] === EMPTY &&
            this.gameBoard[1][1] === BLACK
        )
            scoreB -= 25;
        else if (
            this.gameBoard[0][0] === EMPTY &&
            this.gameBoard[1][1] === WHITE
        )
            scoreW += 25;

        if (
            this.gameBoard[0][7] === EMPTY &&
            this.gameBoard[1][6] === BLACK
        )
            scoreB -= 25;
        else if (
            this.gameBoard[0][7] === EMPTY &&
            this.gameBoard[1][6] === WHITE
        )
            scoreW += 25;

        if (
            this.gameBoard[7][0] === EMPTY &&
            this.gameBoard[6][1] === BLACK
        )
            scoreB -= 25;
        else if (
            this.gameBoard[7][0] === EMPTY &&
            this.gameBoard[6][1] === WHITE
        )
            scoreW += 25;

        if (
            this.gameBoard[7][7] === EMPTY &&
            this.gameBoard[6][6] === BLACK
        )
            scoreB -= 25;
        else if (
            this.gameBoard[7][7] === EMPTY &&
            this.gameBoard[6][6] === WHITE
        )
            scoreW += 25;

        return scoreB - scoreW;
    }

    private getFrontierSquares(letter: number): number[] {
        const frontier: number[] = [];
        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                if (this.gameBoard[row][col] !== letter) continue;
                for (let dr = -1; dr <= 1; dr++) {
                    for (let dc = -1; dc <= 1; dc++) {
                        if (dr === 0 && dc === 0) continue;
                        const nr = row + dr;
                        const nc = col + dc;
                        if (
                            nr >= 0 &&
                            nr < BOARD_SIZE &&
                            nc >= 0 &&
                            nc < BOARD_SIZE &&
                            this.gameBoard[nr][nc] === EMPTY
                        ) {
                            frontier.push(row * BOARD_SIZE + col);
                            break;
                        }
                    }
                }
            }
        }
        return frontier;
    }

    getGameBoard(): number[][] {
        return this.gameBoard;
    }

    getLastMove(): Move {
        return this.lastMove;
    }

    getLastPlayer(): number {
        return this.lastPlayer;
    }
}
