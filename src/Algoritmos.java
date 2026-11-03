package src;

import java.util.ArrayList;

// DFS, BFS, Dijkstra e Kruskal
public class Algoritmos {

    // passo da animacao
    public static class Passo {
        int vertice = -1;
        String rotulo;
        int u = -1, v = -1;

        static Passo vertice(int vertice, String rotulo) {
            Passo p = new Passo();
            p.vertice = vertice;
            p.rotulo = rotulo;
            return p;
        }

        static Passo aresta(int u, int v) {
            Passo p = new Passo();
            p.u = u;
            p.v = v;
            return p;
        }
    }

    // resultado da DFS e BFS
    public static class Busca {
        int[] ordem;     // ordem de visita
        int quantos;
        int[] pai;
        int[] nivel;     // profundidade ou saltos
        ArrayList<Passo> passos = new ArrayList<>();
    }

    public static class ResultadoDijkstra {
        double[] dist;
        int[] pai;
        ArrayList<Passo> passos = new ArrayList<>();
    }

    public static class ArestaKruskal {
        int u, v;
        double peso;
        boolean aceita;
    }

    public static class ResultadoKruskal {
        ArrayList<ArestaKruskal> analisadas = new ArrayList<>();
        ArrayList<ArestaKruskal> escolhidas = new ArrayList<>();
        double custoTotal = 0;
        boolean ligouTodos;
        ArrayList<Passo> passos = new ArrayList<>();
    }

    // DFS com pilha - O(V + E)
    public static Busca dfs(Grafo g, int origem) {
        int n = g.numVertices();
        Busca r = new Busca();
        r.ordem = new int[n];
        r.pai = new int[n];
        r.nivel = new int[n];
        boolean[] visitado = new boolean[n];
        int[] proximo = new int[n];
        int[] pilha = new int[n];
        int topo = 0;

        for (int i = 0; i < n; i++) r.pai[i] = -1;

        visitado[origem] = true;
        r.ordem[r.quantos++] = origem;
        r.passos.add(Passo.vertice(origem, "1"));
        pilha[topo++] = origem;

        while (topo > 0) {
            int u = pilha[topo - 1];
            if (proximo[u] < g.adj.get(u).size()) {
                int v = g.adj.get(u).get(proximo[u]).destino;
                proximo[u]++;
                if (!visitado[v]) {
                    visitado[v] = true;
                    r.pai[v] = u;
                    r.nivel[v] = r.nivel[u] + 1;
                    r.ordem[r.quantos++] = v;
                    r.passos.add(Passo.aresta(u, v));
                    r.passos.add(Passo.vertice(v, "" + r.quantos));
                    pilha[topo++] = v;
                }
            } else {
                topo--; // volta
            }
        }
        return r;
    }

    // becos sem saida
    public static ArrayList<Integer> becosSemSaida(Grafo g) {
        ArrayList<Integer> becos = new ArrayList<>();
        int limite = g.direcionado ? 0 : 1;
        for (int u = 0; u < g.numVertices(); u++) {
            if (g.adj.get(u).size() <= limite) becos.add(u);
        }
        return becos;
    }

    // BFS com fila - O(V + E)
    public static Busca bfs(Grafo g, int origem) {
        int n = g.numVertices();
        Busca r = new Busca();
        r.ordem = new int[n]; // fila
        r.pai = new int[n];
        r.nivel = new int[n];
        boolean[] descoberto = new boolean[n];

        for (int i = 0; i < n; i++) r.pai[i] = -1;

        int inicio = 0;
        descoberto[origem] = true;
        r.ordem[r.quantos++] = origem;
        r.passos.add(Passo.vertice(origem, "0"));

        while (inicio < r.quantos) {
            int u = r.ordem[inicio++];
            for (Grafo.Aresta a : g.adj.get(u)) {
                int v = a.destino;
                if (!descoberto[v]) {
                    descoberto[v] = true;
                    r.pai[v] = u;
                    r.nivel[v] = r.nivel[u] + 1;
                    r.ordem[r.quantos++] = v;
                    r.passos.add(Passo.aresta(u, v));
                    r.passos.add(Passo.vertice(v, "" + r.nivel[v]));
                }
            }
        }
        return r;
    }

    // Dijkstra com heap - O((V + E) log V)
    public static ResultadoDijkstra dijkstra(Grafo g, int origem) {
        int n = g.numVertices();

        for (int u = 0; u < n; u++) {
            for (Grafo.Aresta a : g.adj.get(u)) {
                if (g.peso(a) < 0) {
                    throw new IllegalArgumentException("Dijkstra não aceita peso negativo (aresta "
                            + g.nomes.get(u) + "-" + g.nomes.get(a.destino) + ").");
                }
            }
        }

        ResultadoDijkstra r = new ResultadoDijkstra();
        r.dist = new double[n];
        r.pai = new int[n];
        boolean[] fechado = new boolean[n];
        for (int i = 0; i < n; i++) {
            r.dist[i] = Double.POSITIVE_INFINITY;
            r.pai[i] = -1;
        }

        MinHeap heap = new MinHeap();
        r.dist[origem] = 0;
        heap.inserir(0, origem);

        while (!heap.vazio()) {
            int u = heap.removerMenor();
            if (fechado[u]) continue; // ja processado
            fechado[u] = true;

            if (r.pai[u] != -1) r.passos.add(Passo.aresta(r.pai[u], u));
            r.passos.add(Passo.vertice(u, formatar(r.dist[u])));

            for (Grafo.Aresta a : g.adj.get(u)) {
                double nova = r.dist[u] + g.peso(a);
                if (nova < r.dist[a.destino]) { // relaxamento
                    r.dist[a.destino] = nova;
                    r.pai[a.destino] = u;
                    heap.inserir(nova, a.destino);
                }
            }
        }
        return r;
    }

    // Kruskal com heap e Union-Find - O(E log E)
    public static ResultadoKruskal kruskal(Grafo g) {
        int n = g.numVertices();
        ResultadoKruskal r = new ResultadoKruskal();

        // arestas no heap pelo peso
        ArrayList<ArestaKruskal> arestas = new ArrayList<>();
        MinHeap heap = new MinHeap();
        for (int u = 0; u < n; u++) {
            for (Grafo.Aresta a : g.adj.get(u)) {
                if (g.mostrar(u, a)) {
                    ArestaKruskal e = new ArestaKruskal();
                    e.u = u;
                    e.v = a.destino;
                    e.peso = g.peso(a);
                    heap.inserir(e.peso, arestas.size());
                    arestas.add(e);
                }
            }
        }

        UnionFind grupos = new UnionFind(n);
        while (!heap.vazio() && r.escolhidas.size() < n - 1) {
            ArestaKruskal e = arestas.get(heap.removerMenor());
            e.aceita = grupos.unir(e.u, e.v);
            r.analisadas.add(e);
            if (e.aceita) {
                r.escolhidas.add(e);
                r.custoTotal += e.peso;
                r.passos.add(Passo.aresta(e.u, e.v));
            }
        }
        r.ligouTodos = r.escolhidas.size() == n - 1;
        return r;
    }

    // Union-Find
    static class UnionFind {
        int[] chefe;
        int[] altura;

        UnionFind(int n) {
            chefe = new int[n];
            altura = new int[n];
            for (int i = 0; i < n; i++) chefe[i] = i;
        }

        int acharChefe(int x) {
            while (chefe[x] != x) {
                chefe[x] = chefe[chefe[x]]; // compressao de caminho
                x = chefe[x];
            }
            return x;
        }

        // false se formar ciclo
        boolean unir(int a, int b) {
            int ca = acharChefe(a);
            int cb = acharChefe(b);
            if (ca == cb) return false;
            if (altura[ca] < altura[cb]) {
                chefe[ca] = cb;
            } else if (altura[ca] > altura[cb]) {
                chefe[cb] = ca;
            } else {
                chefe[cb] = ca;
                altura[ca]++;
            }
            return true;
        }
    }

    // caminho ate o destino
    public static ArrayList<Integer> caminho(int[] pai, int destino) {
        ArrayList<Integer> invertido = new ArrayList<>();
        for (int v = destino; v != -1; v = pai[v]) {
            invertido.add(v);
        }
        ArrayList<Integer> caminho = new ArrayList<>();
        for (int i = invertido.size() - 1; i >= 0; i--) {
            caminho.add(invertido.get(i));
        }
        return caminho;
    }

    // tira o .0
    public static String formatar(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) return "" + (long) d;
        return "" + d;
    }
}