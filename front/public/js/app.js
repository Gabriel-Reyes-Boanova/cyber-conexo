import { api } from './api.js';

// ---------- utilidades ----------
const $ = (id) => document.getElementById(id);

/** Cria um elemento só com textContent (nunca innerHTML), evitando XSS. */
function el(tag, { className, text, attrs } = {}, children = []) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  for (const [k, v] of Object.entries(attrs || {})) node.setAttribute(k, v);
  node.append(...children);
  return node;
}

const DIFFICULTY = { INICIANTE: 'Iniciante', INTERMEDIARIO: 'Intermediário', AVANCADO: 'Avançado' };
const TOPIC = { LINUX: 'Linux', SEGURANCA: 'Segurança', MISTO: 'Linux e segurança' };
const NAME_RULE = /^[\p{L}\p{N} _.-]{2,30}$/u;

// Conveniências guardadas só neste navegador (o servidor não confia nelas).
const store = {
  get(key, fallback) {
    try { return JSON.parse(localStorage.getItem(key)) ?? fallback; } catch { return fallback; }
  },
  set(key, value) {
    try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* armazenamento indisponível */ }
  },
};

function formatTime(totalSeconds) {
  const s = Math.max(0, Math.round(totalSeconds));
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
}

function shuffled(list) {
  const copy = [...list];
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

// ---------- estado ----------
const state = {
  puzzles: [],
  puzzle: null,      // tabuleiro em andamento
  gameId: null,
  totalGroups: 4,
  board: [],         // palavras que ainda estão no tabuleiro
  selected: new Set(),
  startedAt: 0,
  timer: null,
  busy: false,
};

// ---------- telas ----------
const SCREENS = ['screenHome', 'screenGame', 'screenResult', 'screenRanking'];

function show(screenId) {
  SCREENS.forEach((id) => { $(id).hidden = id !== screenId; });
  window.scrollTo({ top: 0 });
}

function say(id, text, kind = '') {
  const node = $(id);
  node.textContent = text;
  node.className = (node.className.split(' ')[0] || '') + (kind ? ` ${kind}` : '');
}

// ---------- início ----------
async function loadPuzzles() {
  say('homeMessage', 'Carregando tabuleiros…');
  try {
    state.puzzles = await api.listPuzzles();
    say('homeMessage', '');
    renderPuzzleList();
    renderRankingFilter();
  } catch (e) {
    say('homeMessage', e.message, 'error');
  }
}

function renderPuzzleList() {
  const done = store.get('cc.done', {});
  const list = $('puzzleList');
  list.replaceChildren(...state.puzzles.map((p) => {
    const score = done[p.id];
    const meta = `${DIFFICULTY[p.difficulty] ?? p.difficulty} · ${TOPIC[p.topic] ?? p.topic}`;
    const button = el('button', { className: 'puzzle-row', attrs: { type: 'button' } }, [
      el('span', { className: 'puzzle-main' }, [
        el('span', { className: 'puzzle-title', text: p.title }),
        el('span', { className: 'puzzle-desc', text: p.description }),
      ]),
      el('span', { className: 'puzzle-meta' }, [
        el('span', { text: meta }),
        el('span', { className: score !== undefined ? 'done' : '', text: score !== undefined ? `feito, nota ${score}` : 'novo' }),
      ]),
    ]);
    button.addEventListener('click', () => startGame(p));
    return el('li', {}, [button]);
  }));
}

function readName() {
  const input = $('playerName');
  const name = input.value.trim();
  if (!NAME_RULE.test(name)) {
    say('homeMessage', 'Nome inválido. Use de 2 a 30 letras, números, espaço, ponto, hífen ou sublinhado.', 'error');
    input.focus();
    return null;
  }
  store.set('cc.name', name);
  return name;
}

// ---------- partida ----------
async function startGame(puzzle) {
  if (state.busy) return;
  const name = readName();
  if (!name) return;

  state.busy = true;
  say('homeMessage', 'Iniciando partida…');
  try {
    const game = await api.startGame(name, puzzle.id);
    Object.assign(state, {
      puzzle,
      gameId: game.gameId,
      totalGroups: game.totalGroups,
      board: game.words,
      selected: new Set(),
      startedAt: Date.now(),
    });

    $('gameTitle').textContent = game.puzzleTitle;
    $('gameSubtitle').textContent = `${DIFFICULTY[puzzle.difficulty] ?? ''} · ${TOPIC[puzzle.topic] ?? ''}`;
    $('foundGroups').replaceChildren();
    $('explanations').replaceChildren();
    updateStats({ score: 0, mistakes: 0, groupsFound: 0 });
    say('feedback', 'Selecione quatro palavras que tenham algo em comum.');
    renderBoard();
    startTimer();
    say('homeMessage', '');
    show('screenGame');
  } catch (e) {
    say('homeMessage', e.message, 'error');
  } finally {
    state.busy = false;
  }
}

function renderBoard() {
  const board = $('board');
  board.replaceChildren(...state.board.map((word) => {
    const selected = state.selected.has(word);
    const tile = el('button', {
      className: 'tile' + (selected ? ' selected' : ''),
      text: word,
      attrs: { type: 'button', 'aria-pressed': String(selected) },
    });
    tile.addEventListener('click', () => toggleWord(word));
    return tile;
  }));
  $('guessButton').disabled = state.selected.size !== 4 || state.busy;
}

function toggleWord(word) {
  if (state.selected.has(word)) state.selected.delete(word);
  else if (state.selected.size < 4) state.selected.add(word);
  renderBoard();
}

function updateStats({ score, mistakes, groupsFound }) {
  $('statScore').textContent = score;
  $('statMistakes').textContent = mistakes;
  $('statGroups').textContent = `${groupsFound}/${state.totalGroups}`;
}

function startTimer() {
  stopTimer();
  const tick = () => { $('statTime').textContent = formatTime((Date.now() - state.startedAt) / 1000); };
  tick();
  state.timer = setInterval(tick, 1000);
}

function stopTimer() {
  if (state.timer) clearInterval(state.timer);
  state.timer = null;
}

async function submitGuess() {
  if (state.selected.size !== 4 || state.busy) return;
  state.busy = true;
  $('guessButton').disabled = true;
  const words = [...state.selected];

  try {
    const r = await api.guess(state.gameId, words);
    updateStats(r);

    if (r.correct) {
      addFoundGroup(r, words);
      state.board = state.board.filter((w) => !words.includes(w));
      say('feedback', r.message, 'success');
    } else {
      say('feedback', r.message, r.oneAway ? 'near' : 'error');
    }
    state.selected.clear();
    renderBoard();

    if (r.completed) await finishGame();
  } catch (e) {
    say('feedback', e.message, 'error');
  } finally {
    state.busy = false;
    $('guessButton').disabled = state.selected.size !== 4;
  }
}

function addFoundGroup(result, words) {
  $('foundGroups').append(
    el('div', { className: `found level-${result.level}` }, [
      el('strong', { text: result.category }),
      el('span', { text: words.join(', ') }),
    ]),
  );
  $('explanations').append(
    el('article', { className: `explain level-${result.level}` }, [
      el('h3', { text: result.category }),
      el('p', { text: result.explanation }),
    ]),
  );
}

async function finishGame() {
  stopTimer();
  const info = await api.gameState(state.gameId);
  const done = store.get('cc.done', {});
  done[state.puzzle.id] = info.score;
  store.set('cc.done', done);

  $('resultScore').textContent = info.score;
  $('resultDetails').textContent =
    `Você encontrou ${info.groupsFound} grupos em ${formatTime(info.durationSeconds)} ` +
    `com ${info.mistakes} ${info.mistakes === 1 ? 'erro' : 'erros'}.`;

  const next = nextPuzzle();
  $('nextPuzzleButton').hidden = !next;
  show('screenResult');
  renderPuzzleList();
}

function nextPuzzle() {
  const done = store.get('cc.done', {});
  const idx = state.puzzles.findIndex((p) => p.id === state.puzzle.id);
  const ordered = [...state.puzzles.slice(idx + 1), ...state.puzzles.slice(0, idx)];
  return ordered.find((p) => done[p.id] === undefined) ?? null;
}

// ---------- ranking ----------
function renderRankingFilter(selected = '') {
  const select = $('rankingFilter');
  select.replaceChildren(
    el('option', { text: 'Todos os tabuleiros', attrs: { value: '' } }),
    ...state.puzzles.map((p) => el('option', { text: p.title, attrs: { value: String(p.id) } })),
  );
  select.value = selected;
}

async function openRanking(puzzleId = '') {
  show('screenRanking');
  renderRankingFilter(String(puzzleId ?? ''));
  await loadRanking();
}

async function loadRanking() {
  const puzzleId = $('rankingFilter').value;
  say('rankingMessage', 'Carregando…');
  try {
    const rows = await api.ranking(puzzleId || null, 20);
    $('rankingBody').replaceChildren(...rows.map((r) => el('tr', {}, [
      el('td', { text: String(r.position) }),
      el('td', { text: r.playerName }),
      el('td', { text: r.puzzleTitle }),
      el('td', { text: String(r.score) }),
      el('td', { text: String(r.mistakes) }),
      el('td', { text: r.durationSeconds == null ? '-' : formatTime(r.durationSeconds) }),
    ])));
    say('rankingMessage', rows.length ? '' : 'Ainda não há partidas concluídas. Seja o primeiro!');
  } catch (e) {
    say('rankingMessage', e.message, 'error');
  }
}

// ---------- eventos ----------
$('nameForm').addEventListener('submit', (e) => e.preventDefault());
$('guessButton').addEventListener('click', submitGuess);
$('clearButton').addEventListener('click', () => { state.selected.clear(); renderBoard(); say('feedback', 'Seleção limpa.'); });
$('shuffleButton').addEventListener('click', () => { state.board = shuffled(state.board); renderBoard(); });
$('navHome').addEventListener('click', () => { stopTimer(); show('screenHome'); });
$('navRanking').addEventListener('click', () => { stopTimer(); openRanking(''); });
$('rankingFilter').addEventListener('change', loadRanking);
$('nextPuzzleButton').addEventListener('click', () => { const n = nextPuzzle(); if (n) startGame(n); });
$('resultRankingButton').addEventListener('click', () => openRanking(state.puzzle?.id ?? ''));
$('resultHomeButton').addEventListener('click', () => show('screenHome'));

$('playerName').value = store.get('cc.name', '');
loadPuzzles();
