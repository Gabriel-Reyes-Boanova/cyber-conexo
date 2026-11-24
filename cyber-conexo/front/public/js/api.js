// Camada de acesso à API. Todas as chamadas passam por aqui.
// O Nginx encaminha /api/* para o backend, então usamos caminho relativo (mesma origem).

const BASE = '/api';

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(BASE + path, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new Error('Não foi possível falar com o servidor. Verifique sua conexão.');
  }

  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    // O backend devolve { message }. Erros do próprio Spring/Nginx podem vir com "detail" ou sem corpo.
    if (response.status === 429) {
      throw new Error('Muitas requisições seguidas. Espere um instante e tente de novo.');
    }
    throw new Error(body.message || body.detail || 'Algo deu errado. Tente novamente.');
  }
  return body;
}

export const api = {
  listPuzzles: () => request('/puzzles'),

  startGame: (playerName, puzzleId) =>
    request('/games', { method: 'POST', body: JSON.stringify({ playerName, puzzleId }) }),

  guess: (gameId, words) =>
    request(`/games/${encodeURIComponent(gameId)}/guesses`, { method: 'POST', body: JSON.stringify({ words }) }),

  gameState: (gameId) => request(`/games/${encodeURIComponent(gameId)}`),

  ranking: (puzzleId, limit = 10) => {
    const params = new URLSearchParams({ limit });
    if (puzzleId) params.set('puzzleId', puzzleId);
    return request(`/ranking?${params}`);
  },
};
