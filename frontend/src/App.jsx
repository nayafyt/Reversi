import React, { useState, useEffect, useCallback } from "react";
import Board from "./components/Board.jsx";
import { getState, newGame, makeMove } from "./api.js";

const EMPTY_BOARD = Array.from({ length: 8 }, () => Array(8).fill(0));

export default function App() {
  const [game, setGame] = useState(null);
  const [loading, setLoading] = useState(false);
  const [color, setColor] = useState("B");
  const [level, setLevel] = useState(1);

  const startGame = useCallback(async () => {
    setLoading(true);
    try {
      const state = await newGame(color, level);
      setGame(state);
    } catch (err) {
      console.error("Failed to start game:", err);
    }
    setLoading(false);
  }, [color, level]);

  useEffect(() => {
    startGame();
  }, []);

  const handleCellClick = async (row, col) => {
    if (loading || !game || game.gameOver) return;
    setLoading(true);
    try {
      const state = await makeMove(row, col);
      setGame(state);
    } catch (err) {
      console.error("Move failed:", err);
    }
    setLoading(false);
  };

  const board = game ? game.board : EMPTY_BOARD;
  const availableMoves = game ? game.availableMoves || [] : [];
  const lastMove = game ? game.lastMove : null;

  const getMessage = () => {
    if (!game) return "";
    if (game.gameOver) {
      if (game.winner === "DRAW") return "It's a draw!";
      const winnerName = game.winner === game.playerColor ? "You win!" : "Computer wins!";
      return `Game over — ${winnerName}`;
    }
    if (game.message) return game.message;
    if (loading) return "Thinking...";
    return "Your turn";
  };

  const playerIsBlack = game?.playerColor === "B";
  const playerCount = playerIsBlack ? (game?.blackCount ?? 2) : (game?.whiteCount ?? 2);
  const computerCount = playerIsBlack ? (game?.whiteCount ?? 2) : (game?.blackCount ?? 2);
  const isPlayerTurn = !loading && !game?.gameOver;
  const isComputerTurn = loading && !game?.gameOver;

  return (
    <>
      <h1>Reversi</h1>

      <div className="players-bar">
        <div className={`player-card ${isPlayerTurn ? "active-turn" : ""}`}>
          <div className="player-label">You</div>
          <div className="player-score">
            <span className={`disc-icon ${playerIsBlack ? "black" : "white"}`} />
            <span className="score-number">{playerCount}</span>
          </div>
          <div className="player-color">{playerIsBlack ? "Black" : "White"}</div>
          {isPlayerTurn && <div className="turn-indicator">Your turn</div>}
        </div>

        <div className="vs">VS</div>

        <div className={`player-card computer ${isComputerTurn ? "active-turn" : ""}`}>
          <div className="player-label">Computer</div>
          <div className="player-score">
            <span className={`disc-icon ${playerIsBlack ? "white" : "black"}`} />
            <span className="score-number">{computerCount}</span>
          </div>
          <div className="player-color">{playerIsBlack ? "White" : "Black"}</div>
          {isComputerTurn && <div className="turn-indicator thinking">Thinking...</div>}
        </div>
      </div>

      <Board
        board={board}
        availableMoves={availableMoves}
        lastMove={lastMove}
        onCellClick={handleCellClick}
        disabled={loading || (game && game.gameOver)}
      />

      <div className={`message ${game?.gameOver ? "game-over" : ""}`}>
        {getMessage()}
      </div>

      <div className="controls">
        <label>
          Color
          <select value={color} onChange={(e) => setColor(e.target.value)}>
            <option value="B">Black (first)</option>
            <option value="W">White (second)</option>
          </select>
        </label>
        <label>
          Difficulty
          <select value={level} onChange={(e) => setLevel(Number(e.target.value))}>
            <option value={1}>Easy</option>
            <option value={2}>Medium</option>
            <option value={3}>Hard</option>
          </select>
        </label>
        <button className="btn" onClick={startGame} disabled={loading}>
          New Game
        </button>
      </div>
    </>
  );
}
