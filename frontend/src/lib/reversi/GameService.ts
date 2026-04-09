import { Board } from "./Board";
import { Player } from "./Player";
import { GameState, GameConfig, BLACK, WHITE, Move } from "./types";

export class GameService {
    private board: Board | null = null;
    private computer: Player | null = null;
    private playerLetter: number = BLACK;
    private computerLetter: number = WHITE;
    private skipPlayer: number = 0;
    private skipComputer: number = 0;
    private gameOver: boolean = false;
    private message: string | null = null;

    newGame(config: GameConfig): GameState {
        const { playerColor, level } = config;
        const validLevel = Math.max(1, Math.min(3, level));

        if (playerColor === "W") {
            this.playerLetter = WHITE;
            this.computerLetter = BLACK;
        } else {
            this.playerLetter = BLACK;
            this.computerLetter = WHITE;
        }

        this.board = new Board();
        this.computer = new Player(validLevel + 1, this.computerLetter);
        this.skipPlayer = 0;
        this.skipComputer = 0;
        this.gameOver = false;
        this.message = null;

        if (this.computerLetter === BLACK) {
            this.computerMove();
        }

        return this.buildState();
    }

    playerMove(row: number, col: number): GameState {
        if (this.gameOver) {
            this.message = "Game is already over.";
            return this.buildState();
        }

        const available = this.board!.getAvailableMoves(this.playerLetter);
        const valid = available.some((m) => m.row === row && m.col === col);

        if (!valid) {
            this.message = "Invalid move.";
            return this.buildState();
        }

        this.skipPlayer = 0;
        this.board!.makeMove(row, col, this.playerLetter);
        this.board!.flipDiscs({ row, col, value: this.playerLetter });
        this.message = null;

        if (this.checkGameOver()) return this.buildState();

        this.computerMove();

        if (this.checkGameOver()) return this.buildState();

        while (
            !this.gameOver &&
            this.board!.getAvailableMoves(this.playerLetter).length === 0
        ) {
            this.skipPlayer++;
            if (this.skipPlayer >= 1 && this.skipComputer >= 1) {
                this.gameOver = true;
                break;
            }
            this.message =
                "No moves available for you. Computer plays again.";
            this.computerMove();
            if (this.checkGameOver()) break;
        }

        return this.buildState();
    }

    getState(): GameState {
        return this.buildState();
    }

    private computerMove(): void {
        if (
            this.board!.getAvailableMoves(this.computerLetter).length === 0
        ) {
            this.skipComputer++;
            if (this.skipPlayer >= 1 && this.skipComputer >= 1) {
                this.gameOver = true;
            }
            this.message = "Computer has no moves and skips.";
            return;
        }

        this.skipComputer = 0;
        const aiMove =
            this.computerLetter === BLACK
                ? this.computer!.max(new Board(this.board!), 0)
                : this.computer!.min(new Board(this.board!), 0);

        if (aiMove.row < 0 || aiMove.col < 0) {
            this.skipComputer++;
            if (this.skipPlayer >= 1 && this.skipComputer >= 1)
                this.gameOver = true;
            this.message = "Computer skips.";
            return;
        }

        this.board!.makeMove(aiMove.row, aiMove.col, this.computerLetter);
        this.board!.flipDiscs({
            row: aiMove.row,
            col: aiMove.col,
            value: this.computerLetter,
        });
    }

    private checkGameOver(): boolean {
        if (this.board!.isTerminal()) {
            this.gameOver = true;
            return true;
        }
        return false;
    }

    private buildState(): GameState {
        const boardArray = this.board!.getGameBoard();
        let bCount = 0;
        let wCount = 0;

        for (let r = 0; r < 8; r++) {
            for (let c = 0; c < 8; c++) {
                if (boardArray[r][c] === BLACK) bCount++;
                else if (boardArray[r][c] === WHITE) wCount++;
            }
        }

        let winner: string | undefined;
        if (this.gameOver) {
            if (bCount > wCount) winner = "B";
            else if (wCount > bCount) winner = "W";
            else winner = "Draw";
        }

        const availableMoves = this.board!.getAvailableMoves(
            this.playerLetter
        );

        return {
            board: boardArray,
            playerColor: this.playerLetter === BLACK ? "B" : "W",
            blackCount: bCount,
            whiteCount: wCount,
            gameOver: this.gameOver,
            winner,
            availableMoves,
            message: this.message || undefined,
            lastMove: this.board!.getLastMove(),
        };
    }
}
