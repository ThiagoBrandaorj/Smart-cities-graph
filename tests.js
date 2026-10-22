const { Graph, dfs, bfs, dijkstra, kruskal } = require('./graph.js');
const g = new Graph(false, true);
[['O','A',2],['O','B',5],['O','C',4],['A','B',2],['A','D',7],['A','F',12],['B','C',1],['B','D',4],['B','E',3],['C','E',4],['D','E',4],['D','T',5],['E','T',7],['F','T',3]].forEach(e => g.addEdge(...e));
const d = dijkstra(g, 'O'), k = kruskal(g);
console.log('Dijkstra:', Object.fromEntries(d.dist), '| caminho F:', d.path('F').join('>'));
console.log('Kruskal total:', k.total, k.chosen.map(e => e.u + e.v).join(','));
console.log('DFS:', dfs(g, 'O').order.join(''), '| BFS:', bfs(g, 'O').order.join(''));
