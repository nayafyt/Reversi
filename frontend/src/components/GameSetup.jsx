import React from "react";

export default function GameSetup({
    color,
    setColor,
    level,
    setLevel,
    onStartGame,
    loading,
}) {
    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h2>Welcome to Reversi</h2>
                </div>

                <div className="modal-instructions">
                    <p>
                        <strong>Goal:</strong> Capture your opponent's pieces by trapping them between your own.
                        The player with the most pieces at the end wins!
                    </p>
                </div>

                <div className="modal-options">
                    <div className="option-group">
                        <label htmlFor="color-select">
                            <span className="option-label">Play as:</span>
                            <select
                                id="color-select"
                                value={color}
                                onChange={(e) => setColor(e.target.value)}
                            >
                                <option value="B">Black (goes first)</option>
                                <option value="W">White (goes second)</option>
                            </select>
                        </label>
                    </div>

                    <div className="option-group">
                        <label htmlFor="difficulty-select">
                            <span className="option-label">Difficulty:</span>
                            <select
                                id="difficulty-select"
                                value={level}
                                onChange={(e) => setLevel(Number(e.target.value))}
                            >
                                <option value={1}>Easy</option>
                                <option value={2}>Medium</option>
                                <option value={3}>Hard</option>
                            </select>
                        </label>
                    </div>
                </div>

                <div className="modal-footer">
                    <button
                        className="btn btn-primary"
                        onClick={onStartGame}
                        disabled={loading}
                    >
                        {loading ? "Starting..." : "Start Game"}
                    </button>
                </div>
            </div>
        </div>
    );
}
