import { Board } from "./Board";
import { Move, BLACK, WHITE } from "./types";

interface MoveWithValue extends Move {
    value: number;
}

export class Player {
    private maxDepth: number;

    constructor(maxDepth: number, _playerLetter: number) {
        this.maxDepth = maxDepth;
    }

    max(board: Board, depth: number): MoveWithValue {
        if (board.isTerminal() || depth === this.maxDepth) {
            const lastMove = board.getLastMove();
            return {
                row: lastMove.row,
                col: lastMove.col,
                value: board.evaluate(),
            };
        }

        const children = board.getChildren(BLACK);

        let maxMove: MoveWithValue = {
            row: -1,
            col: -1,
            value: Number.MIN_SAFE_INTEGER,
        };

        for (const child of children) {
            const move = this.min(child, depth + 1);

            if (this.hasNoPosition(move)) {
                continue;
            }

            if (move.value >= maxMove.value) {
                if (move.value === maxMove.value) {
                    if (Math.random() < 0.5) {
                        const childLastMove = child.getLastMove();
                        maxMove = {
                            row: childLastMove.row,
                            col: childLastMove.col,
                            value: move.value,
                        };
                    }
                } else {
                    const childLastMove = child.getLastMove();
                    maxMove = {
                        row: childLastMove.row,
                        col: childLastMove.col,
                        value: move.value,
                    };
                }
            }
        }

        return maxMove;
    }

    min(board: Board, depth: number): MoveWithValue {
        if (board.isTerminal() || depth === this.maxDepth) {
            const lastMove = board.getLastMove();
            return {
                row: lastMove.row,
                col: lastMove.col,
                value: board.evaluate(),
            };
        }

        const children = board.getChildren(WHITE);

        let minMove: MoveWithValue = {
            row: -1,
            col: -1,
            value: Number.MAX_SAFE_INTEGER,
        };

        for (const child of children) {
            const move = this.max(child, depth + 1);

            if (this.hasNoPosition(move)) {
                continue;
            }

            if (move.value <= minMove.value) {
                if (move.value === minMove.value) {
                    if (Math.random() < 0.5) {
                        const childLastMove = child.getLastMove();
                        minMove = {
                            row: childLastMove.row,
                            col: childLastMove.col,
                            value: move.value,
                        };
                    }
                } else {
                    const childLastMove = child.getLastMove();
                    minMove = {
                        row: childLastMove.row,
                        col: childLastMove.col,
                        value: move.value,
                    };
                }
            }
        }

        return minMove;
    }

    private hasNoPosition(move: MoveWithValue): boolean {
        return move.col < 0 && move.row < 0;
    }
}
