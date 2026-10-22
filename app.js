const $ = s => document.querySelector(s), cv = $('#cv'), ctx = cv.getContext('2d'), R = 22;
let g = new Graph(false, true), pos = new Map(), mode = 'node', sel = null, drag = null, timer = null, hl = { n: new Map(), e: new Set() };
const esc = s => String(s).replace(/[&<>]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' }[c]));
const hit = (x, y) => { for (const [n, p] of pos) if (Math.hypot(p.x - x, p.y - y) <= R) return n; return null; };
function nextName() { for (let i = 0; i < 26; i++) { const c = String.fromCharCode(65 + i); if (!pos.has(c)) return c; } let i = 27; while (pos.has('V' + i)) i++; return 'V' + i; }
function clearHl() { clearInterval(timer); hl = { n: new Map(), e: new Set() }; }
function refresh() { clearHl(); renderStruct(); fillSel(); draw(); }

function ends(u, v) {
  const a = pos.get(u), b = pos.get(v), dx = b.x - a.x, dy = b.y - a.y, L = Math.hypot(dx, dy) || 1, ux = dx / L, uy = dy / L;
  const o = g.directed && g.hasEdge(v, u) ? 7 : 0, px = -uy * o, py = ux * o;
  return [{ x: a.x + ux * R + px, y: a.y + uy * R + py }, { x: b.x - ux * R + px, y: b.y - uy * R + py }];
}
function arrow(a, b, col) {
  const t = Math.atan2(b.y - a.y, b.x - a.x);
  ctx.fillStyle = col; ctx.beginPath(); ctx.moveTo(b.x, b.y);
  ctx.lineTo(b.x - 14 * Math.cos(t - .4), b.y - 14 * Math.sin(t - .4)); ctx.lineTo(b.x - 14 * Math.cos(t + .4), b.y - 14 * Math.sin(t + .4)); ctx.fill();
}
function draw() {
  ctx.clearRect(0, 0, cv.width, cv.height); ctx.fillStyle = '#fafaf7'; ctx.fillRect(0, 0, cv.width, cv.height);
  ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
  for (const e of g.edges()) {
    const [a, b] = ends(e.u, e.v), on = hl.e.has(g.key(e.u, e.v)), col = on ? '#0F9D6B' : '#888';
    ctx.strokeStyle = col; ctx.lineWidth = on ? 4 : 1.8; ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke();
    if (g.directed) arrow(a, b, col);
    if (g.weighted) {
      const mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2; ctx.font = 'bold 14px sans-serif';
      const w = ctx.measureText(e.w).width + 8; ctx.fillStyle = '#fafaf7'; ctx.fillRect(mx - w / 2, my - 10, w, 20);
      ctx.fillStyle = on ? '#0a6b49' : '#333'; ctx.fillText(e.w, mx, my);
    }
  }
  for (const [n, p] of pos) {
    const l = hl.n.get(n), on = l !== undefined;
    ctx.beginPath(); ctx.arc(p.x, p.y, R, 0, 7); ctx.fillStyle = n === sel ? '#ffe08a' : on ? '#bfe9d8' : '#fff'; ctx.fill();
    ctx.lineWidth = 2; ctx.strokeStyle = on ? '#0F9D6B' : '#333'; ctx.stroke();
    ctx.fillStyle = '#111'; ctx.font = 'bold 16px sans-serif'; ctx.fillText(n, p.x, p.y);
    if (on && l !== '') { ctx.fillStyle = '#0a6b49'; ctx.font = 'bold 13px sans-serif'; ctx.fillText(l, p.x, p.y - R - 9); }
  }
}
function resize() { cv.width = cv.clientWidth; cv.height = 520; draw(); }
addEventListener('resize', resize);

function distSeg(px, py, a, b) {
  const dx = b.x - a.x, dy = b.y - a.y, t = Math.max(0, Math.min(1, ((px - a.x) * dx + (py - a.y) * dy) / (dx * dx + dy * dy || 1)));
  return Math.hypot(px - a.x - t * dx, py - a.y - t * dy);
}
function addEdgeUI(u, v) {
  let w = 1;
  if (g.weighted) { w = parseFloat((prompt(`Peso da aresta ${u} → ${v}:`, '1') || '').replace(',', '.')); if (isNaN(w)) return false; }
  g.addEdge(u, v, w); return true;
}
const pt = ev => { const r = cv.getBoundingClientRect(); return [ev.clientX - r.left, ev.clientY - r.top]; };
cv.onmousedown = ev => {
  const [x, y] = pt(ev), n = hit(x, y);
  if (mode === 'node') { if (!n) { const nm = nextName(); g.addVertex(nm); pos.set(nm, { x, y }); refresh(); } }
  else if (mode === 'edge') {
    if (!n) sel = null; else if (!sel) sel = n; else { const ok = n !== sel && addEdgeUI(sel, n); sel = null; if (ok) return refresh(); }
    draw();
  } else if (mode === 'move') { if (n) drag = n; }
  else if (mode === 'del') {
    if (n) { g.removeVertex(n); pos.delete(n); refresh(); }
    else for (const e of g.edges()) { const [a, b] = ends(e.u, e.v); if (distSeg(x, y, a, b) < 8) { g.removeEdge(e.u, e.v); refresh(); break; } }
  }
};
cv.onmousemove = ev => { if (drag) { const [x, y] = pt(ev); pos.set(drag, { x, y }); draw(); } };
addEventListener('mouseup', () => drag = null);
cv.ondblclick = ev => {
  const n = hit(...pt(ev)); if (!n) return;
  const nn = (prompt('Novo nome do vértice:', n) || '').trim();
  if (!nn || nn === n) return; if (pos.has(nn)) return alert('Já existe um vértice com esse nome.');
  g.rename(n, nn); pos.set(nn, pos.get(n)); pos.delete(n); sel = null; refresh();
};

function renderStruct() {
  let h = '';
  for (const [u, l] of g.adj) h += `<tr><td class="k">${esc(u)}</td><td>${l.map(e => `<span class="chip">${esc(e.to)}${g.weighted ? `<small>${e.w}</small>` : ''}</span>`).join('<span class="ar">→</span>') || '<i>∅</i>'}</td></tr>`;
  $('#struct').innerHTML = h ? `<table>${h}</table>` : '<p class="mut">Grafo vazio.</p>';
}
function fillSel() {
  const s = $('#src'), old = s.value; s.innerHTML = [...pos.keys()].map(v => `<option>${esc(v)}</option>`).join('');
  if (pos.has(old)) s.value = old;
}
const tbl = (h, rows) => `<table class="t"><tr>${h.map(x => `<th>${x}</th>`).join('')}</tr>${rows.map(r => `<tr>${r.map(c => `<td>${c}</td>`).join('')}</tr>`).join('')}</table>`;
function play(seq) {
  clearHl(); let i = 0;
  timer = setInterval(() => {
    if (i >= seq.length) return clearInterval(timer);
    const s = seq[i++];
    if (s.n !== undefined) hl.n.set(s.n, s.l);
    if (s.e) { hl.e.add(g.key(...s.e)); for (const v of s.e) if (!hl.n.has(v)) hl.n.set(v, ''); }
    draw();
  }, 450);
}
function show(t, h) { $('#res').innerHTML = `<h3>${t}</h3>${h}`; }
function run(a) {
  if (!pos.size) return show('Aviso', '<p>Desenhe um grafo no quadro primeiro.</p>');
  const s = $('#src').value;
  try {
    if (a === 'dfs') {
      const r = dfs(g, s);
      show(`DFS a partir de ${esc(s)}`, `<p><b>Ordem de visita:</b> ${r.order.join(' → ')}</p><pre>${esc(r.lines.join('\n'))}</pre><p><b>Conectividade:</b> ${r.unreached.length ? `${r.unreached.join(', ')} não alcançado(s) a partir de ${esc(s)}` : 'todos os vértices são alcançáveis a partir de ' + esc(s)}.</p><p><b>Becos sem saída:</b> ${r.dead.join(', ') || 'nenhum'}</p><p class="mut">Complexidade: O(V+E)</p>`);
      play(r.seq);
    } else if (a === 'bfs') {
      const r = bfs(g, s);
      show(`BFS a partir de ${esc(s)}`, `<p><b>Fila de execução:</b> ${r.order.join(', ')}</p>${tbl(['Vértice', 'Saltos'], r.order.map(v => [v, r.dist.get(v)]))}<p>${r.unreached.length ? 'Não alcançados: ' + r.unreached.join(', ') : 'Todos alcançados.'}</p><p class="mut">Menor caminho em nº de arestas (sem pesos). Complexidade: O(V+E)</p>`);
      play(r.seq);
    } else if (a === 'dij') {
      const r = dijkstra(g, s);
      show(`Dijkstra a partir de ${esc(s)}`, tbl(['Vértice', 'Distância', 'Caminho'], [...r.dist].map(([v, d]) => [v, d === Infinity ? '∞' : d, d === Infinity ? 'inalcançável' : r.path(v).join(' → ')])) + '<p class="mut">Complexidade: O((V+E) log V), heap binário</p>');
      play(r.seq);
    } else {
      const r = kruskal(g);
      show('Kruskal (árvore geradora mínima)', `${g.directed ? '<p class="mut">Grafo direcionado: arestas tratadas como não-direcionadas.</p>' : ''}${tbl(['Aresta', 'Peso', 'Decisão'], r.log.map(e => [`${e.u}–${e.v}`, e.c, e.ok ? '✔ aceita' : '✘ forma ciclo']))}<p><b>Arestas escolhidas:</b> ${r.chosen.map(e => `${e.u}–${e.v}`).join(', ') || 'nenhuma'}</p><p><b>Custo total: ${r.total}</b>${r.spanning ? '' : ' (grafo desconexo: floresta geradora mínima)'}</p><p class="mut">Complexidade: O(E log E), Union-Find</p>`);
      play(r.seq);
    }
  } catch (e) { show('Erro', `<p class="err">${esc(e.message)}</p>`); }
}
document.querySelectorAll('[data-a]').forEach(b => b.onclick = () => run(b.dataset.a));
const hints = { node: 'Clique em um espaço vazio para criar um vértice. Duplo clique em um vértice para renomear.', edge: 'Clique no vértice de origem e depois no de destino para criar a aresta.', move: 'Arraste os vértices para reorganizar o desenho.', del: 'Clique em um vértice ou em uma aresta para apagá-lo.' };
document.querySelectorAll('[name=mode]').forEach(r => r.onchange = () => { mode = r.value; sel = null; $('#hint').textContent = hints[mode]; draw(); });
$('#dir').onchange = e => {
  const old = g; g = new Graph(e.target.checked, old.weighted);
  for (const v of old.adj.keys()) g.addVertex(v); for (const x of old.edges()) g.addEdge(x.u, x.v, x.w); refresh();
};
$('#wt').onchange = e => { g.weighted = e.target.checked; refresh(); };
$('#clear').onclick = () => { g = new Graph($('#dir').checked, $('#wt').checked); pos.clear(); $('#res').innerHTML = ''; refresh(); };
$('#demo').onclick = () => {
  $('#dir').checked = false; $('#wt').checked = true; g = new Graph(false, true); pos.clear();
  const P = { O: [.08, .45], A: [.3, .13], F: [.8, .13], B: [.36, .5], D: [.62, .5], T: [.9, .52], C: [.2, .88], E: [.52, .88] };
  for (const k in P) { g.addVertex(k); pos.set(k, { x: P[k][0] * cv.width, y: P[k][1] * cv.height }); }
  [['O', 'A', 2], ['O', 'B', 5], ['O', 'C', 4], ['A', 'B', 2], ['A', 'D', 7], ['A', 'F', 12], ['B', 'C', 1], ['B', 'D', 4], ['B', 'E', 3], ['C', 'E', 4], ['D', 'E', 4], ['D', 'T', 5], ['E', 'T', 7], ['F', 'T', 3]].forEach(e => g.addEdge(...e));
  $('#res').innerHTML = ''; refresh();
};
$('#end').onclick = () => { if (confirm('Finalizar a aplicação?')) document.body.innerHTML = '<div class="bye"><h1>Aplicação finalizada</h1><p>SmartNet Analyzer encerrado.</p><button onclick="location.reload()">Reiniciar</button></div>'; };
resize(); renderStruct();
