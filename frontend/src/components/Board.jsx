import React from "react";

export default function Board({ board, availableMoves, lastMove, onCellClick, disabled }) {
  const isAvailable = (r, c) =>
    availableMoves.some(([mr, mc]) => mr === r && mc === c);

  const isLastMove = (r, c) =>
    lastMove && lastMove[0] === r && lastMove[1] === c;

  return (
    <div className="board">
      {board.map((row, r) =>
        row.map((cell, c) => {
          const available = isAvailable(r, c);
          const classes = [
            "cell",
            available && !disabled ? "available" : "",
            isLastMove(r, c) ? "last-move" : "",
          ]
            .filter(Boolean)
            .join(" ");

          return (
            <div
              key={`${r}-${c}`}
              className={classes}
              onClick={() => available && !disabled && onCellClick(r, c)}
            >
              {cell === -1 && <div className="disc black" />}
              {cell === 1 && <div className="disc white" />}
            </div>
          );
        })
      )}
    </div>
  );
}
