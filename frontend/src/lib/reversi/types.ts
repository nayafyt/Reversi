// Game constants
export const BOARD_SIZE = 8;
export const WHITE = 1;
export const BLACK = -1;
export const EMPTY = 0;

// Types
export interface Move {
    row: number;
    col: number;
    value?: number;
}

export interface GameState {
    board: number[][];
    playerColor: string;
    blackCount: number;
    whiteCount: number;
    gameOver: boolean;
    winner?: string;
    availableMoves: Move[];
    message?: string;
    lastMove?: Move;
}

export interface GameConfig {
    playerColor: "W" | "B";
    level: number;
}
