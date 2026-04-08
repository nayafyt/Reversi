const API = "/api/game";

async function request(path, options = {}) {
  const res = await fetch(`${API}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) throw new Error(`API error: ${res.status}`);
  return res.json();
}

export function newGame(playerColor, level) {
  return request("/new", {
    method: "POST",
    body: JSON.stringify({ playerColor, level }),
  });
}

export function makeMove(row, col) {
  return request("/move", {
    method: "POST",
    body: JSON.stringify({ row, col }),
  });
}
