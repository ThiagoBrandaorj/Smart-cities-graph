/* SmartNet Analyzer - estrutura de dados e algoritmos (implementados do zero) */
class Graph {
  // Lista de adjacência: Map(vértice -> array de {to, w})
  constructor(directed = false, weighted = true) { this.directed = directed; this.weighted = weighted; this.adj = new Map(); }
  W(e) { return this.weighted ? e.w : 1; }
  key(u, v) { return this.directed ? u + '>' + v : [u, v].sort().join('|'); }
  addVertex(n) { if (!this.adj.has(n)) this.adj.set(n, []); }
  hasEdge(u, v) { return this.adj.has(u) && this.adj.get(u).some(e => e.to === v); }
  _set(u, v, w) { const l = this.adj.get(u), e = l.find(x => x.to === v); if (e) e.w = w; else l.push({ to: v, w }); }
  addEdge(u, v, w = 1) { if (u === v) return; this.addVertex(u); this.addVertex(v); this._set(u, v, w); if (!this.directed) this._set(v, u, w); }
  removeEdge(u, v) {
    this.adj.set(u, this.adj.get(u).filter(e => e.to !== v));
    if (!this.directed) this.adj.set(v, this.adj.get(v).filter(e => e.to !== u));
  }
  removeVertex(n) { this.adj.delete(n); for (const [u, l] of this.adj) this.adj.set(u, l.filter(e => e.to !== n)); }
  rename(a, b) {
    const m = new Map();
    for (const [u, l] of this.adj) m.set(u === a ? b : u, l.map(e => ({ to: e.to === a ? b : e.to, w: e.w })));
    this.adj = m;
  }
  edges() {
    const r = [], seen = new Set();
    for (const [u, l] of this.adj) for (const { to, w } of l) {
      const k = this.key(u, to);
      if (!this.directed) { if (seen.has(k)) continue; seen.add(k); }
      r.push({ u, v: to, w });
    }
    return r;
  }
}

// DFS recursiva: O(V+E)
function dfs(g, s) {
  const vis = new Set(), order = [], seq = [], lines = [];
  (function go(u, p, d) {
    vis.add(u); order.push(u);
    if (p) seq.push({ e: [p, u] });
    seq.push({ n: u, l: String(order.length) });
    lines.push('    '.repeat(d) + (p ? '└─ ' : '') + u);
    for (const { to } of g.adj.get(u)) if (!vis.has(to)) go(to, u, d + 1);
  })(s, null, 0);
  const unreached = [...g.adj.keys()].filter(v => !vis.has(v));
  const dead = [...g.adj.keys()].filter(v => g.adj.get(v).length <= (g.directed ? 0 : 1));
  return { order, seq, lines, unreached, dead };
}

// BFS com fila (índice, dequeue O(1)): O(V+E)
function bfs(g, s) {
  const dist = new Map([[s, 0]]), q = [s], seq = [{ n: s, l: '0' }];
  for (let i = 0; i < q.length; i++) {
    const u = q[i];
    for (const { to } of g.adj.get(u)) if (!dist.has(to)) {
      dist.set(to, dist.get(u) + 1); q.push(to);
      seq.push({ e: [u, to] }, { n: to, l: String(dist.get(to)) });
    }
  }
  const unreached = [...g.adj.keys()].filter(v => !dist.has(v));
  return { order: q, dist, seq, unreached };
}

class MinHeap {
  constructor() { this.a = []; }
  get size() { return this.a.length; }
  push(x) {
    const a = this.a; a.push(x); let i = a.length - 1;
    while (i > 0) { const p = (i - 1) >> 1; if (a[p][0] <= a[i][0]) break; [a[p], a[i]] = [a[i], a[p]]; i = p; }
  }
  pop() {
    const a = this.a, top = a[0], last = a.pop();
    if (a.length) {
      a[0] = last; let i = 0;
      for (;;) {
        let m = i; const c = 2 * i + 1;
        if (c < a.length && a[c][0] < a[m][0]) m = c;
        if (c + 1 < a.length && a[c + 1][0] < a[m][0]) m = c + 1;
        if (m === i) break; [a[m], a[i]] = [a[i], a[m]]; i = m;
      }
    }
    return top;
  }
}

// Dijkstra com heap binário: O((V+E) log V)
function dijkstra(g, s) {
  for (const e of g.edges()) if (g.W(e) < 0) throw new Error('Dijkstra não aceita pesos negativos (aresta ' + e.u + '–' + e.v + ').');
  const dist = new Map(), prev = new Map(), done = new Set(), seq = [], h = new MinHeap();
  for (const v of g.adj.keys()) dist.set(v, Infinity);
  dist.set(s, 0); h.push([0, s]);
  while (h.size) {
    const [d, u] = h.pop();
    if (done.has(u)) continue;
    done.add(u);
    if (prev.has(u)) seq.push({ e: [prev.get(u), u] });
    seq.push({ n: u, l: String(d) });
    for (const e of g.adj.get(u)) {
      const nd = d + g.W(e);
      if (nd < dist.get(e.to)) { dist.set(e.to, nd); prev.set(e.to, u); h.push([nd, e.to]); }
    }
  }
  const path = v => { if (dist.get(v) === Infinity) return null; const p = [v]; while (prev.has(p[0])) p.unshift(prev.get(p[0])); return p; };
  return { dist, path, seq };
}

// Kruskal com Union-Find (compressão de caminho + união por rank): O(E log E)
function kruskal(g) {
  const parent = new Map(), rank = new Map();
  for (const v of g.adj.keys()) { parent.set(v, v); rank.set(v, 0); }
  const find = x => { while (parent.get(x) !== x) { parent.set(x, parent.get(parent.get(x))); x = parent.get(x); } return x; };
  const union = (a, b) => {
    a = find(a); b = find(b); if (a === b) return false;
    if (rank.get(a) < rank.get(b)) [a, b] = [b, a];
    parent.set(b, a); if (rank.get(a) === rank.get(b)) rank.set(a, rank.get(a) + 1); return true;
  };
  const sorted = g.edges().map(e => ({ ...e, c: g.W(e) })).sort((x, y) => x.c - y.c || x.u.localeCompare(y.u) || x.v.localeCompare(y.v));
  const log = [], chosen = [], seq = []; let total = 0;
  for (const e of sorted) {
    const ok = union(e.u, e.v);
    log.push({ ...e, ok });
    if (ok) { chosen.push(e); total += e.c; seq.push({ e: [e.u, e.v] }); }
  }
  return { log, chosen, total, spanning: chosen.length === g.adj.size - 1, seq };
}
if (typeof module !== 'undefined') module.exports = { Graph, dfs, bfs, dijkstra, kruskal };
